# RoadReady backend (Spring Boot 3, MySQL)

## Run
1. MySQL running on `localhost:3306`; credentials are in `src/main/resources/application.properties`
   (`roadready_db` is created automatically, tables are created/updated by Hibernate: `ddl-auto=update`).
2. `./mvnw spring-boot:run` (or run `RoadreadyBackendApplication` from your IDE). Server: http://localhost:8080
3. Register users from the frontend sign-up pages (customer, rental agent, admin).

## Car pictures (multipart upload, same as CozyHeaven)
`POST /api/car/{id}/upload` receives the picture as `multipart/form-data` (part name `file`), copies it into the
frontend's `public/images/cars` folder and stores only the file name in `car.image_url`. The frontend (Vite) then shows it as
`/images/cars/<file name>`.

The folder is the `uploadDir` constant at the top of `CarService.java`:
`../roadready_frontend/public/images/cars/` (relative to the folder the backend is started from), so keep
`roadready_backend` and `roadready_frontend` next to each other. If your folders are elsewhere, change that constant
to the full path, e.g. `C:/projects/roadready_frontend/public/images/cars/`.

## Pagination and filtering
Paged lists return `{ "list": [...], "paginationInfo": { "totalPages", "totalRecords", "numRecordsInPage" } }`
and take `page` (0-based) and `size` (1-100). Filters are optional query parameters applied in the JPQL query:

| Endpoint | Filters |
|---|---|
| `GET /api/car/filter` (also `/api/car/all`) | brandId, locationId, fuelType, transmission, vehicleType, minPrice, maxPrice, status, search |
| `GET /api/car/api/rental-agent/my-cars` | status, search |
| `GET /api/reservation/my` | status |
| `GET /api/reservation/admin/all` | status, search |
| `GET /api/admin/reservation/cancellation-requests` | - |
| `GET /api/payment/my` | - |
| `GET /api/admin/payment/all` (completed payments only) | paymentMethod, fromDate, toDate, search |
| `GET /api/review/car/{carId}` | - |
| `GET /api/admin/user/all` | role, search |

## Cancellation and refund workflow
1. Customer `PUT /api/reservation/{id}/cancel`: a **paid** reservation becomes `CANCELLATION_REQUESTED` (nothing refunded yet);
   a reservation that was never paid has nothing to refund and is cancelled immediately. Duplicate requests are refused.
2. Admin sees it in `GET /api/admin/reservation/cancellation-requests` (or the reservations list with `status=CANCELLATION_REQUESTED`).
3. Admin `PUT /api/admin/reservation/{id}/cancellation/approve`: reservation -> `CANCELLED`, its `COMPLETED` payment -> `REFUNDED`
   (`refundAmount`, `refundedAt` filled). Approving twice is refused (row lock + status check).
   `PUT .../cancellation/reject` puts the reservation back to `CONFIRMED`.

## Schema additions (applied automatically by `ddl-auto=update`)
`payment.refund_amount`, `payment.refunded_at`, `reservation.cancellation_requested_at` (all nullable),
unique constraint on `review.reservation_id`, new reservation status value `CANCELLATION_REQUESTED` (stored as text).
