
package com.springboot.roadready.service;

import com.springboot.roadready.dto.request.PaymentRequestDto;
import com.springboot.roadready.dto.response.PaymentFilterDto;
import com.springboot.roadready.dto.response.PaymentResponseDto;
import com.springboot.roadready.enums.PaymentMethod;
import com.springboot.roadready.enums.PaymentStatus;
import com.springboot.roadready.enums.ReservationStatus;
import com.springboot.roadready.exception.InvalidCallException;
import com.springboot.roadready.model.Car;
import com.springboot.roadready.model.Customer;
import com.springboot.roadready.model.Payment;
import com.springboot.roadready.model.Reservation;
import com.springboot.roadready.model.User;
import com.springboot.roadready.repository.PaymentRepository;
import com.springboot.roadready.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    private Reservation getReservation() {

        User user = new User();
        user.setUsername("customer1");

        Customer customer = new Customer();
        customer.setUser(user);

        Car car = new Car();
        car.setModel("Honda City");

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setCustomer(customer);
        reservation.setCar(car);
        reservation.setTotalAmount(new BigDecimal("5000"));
        reservation.setStatus(ReservationStatus.PENDING);

        return reservation;
    }

    @Test
    public void createPaymentTest() {

        Reservation reservation = getReservation();

        PaymentRequestDto dto = new PaymentRequestDto(
                1L,
                new BigDecimal("5000"),
                PaymentMethod.UPI
        );

        when(reservationRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(reservation));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponseDto result =
                paymentService.createPayment(dto, "customer1");

        assertNotNull(result);
        assertEquals(1L, result.reservationId());
        assertEquals("Honda City", result.carModel());
        assertEquals("customer1", result.customerUsername());
        assertEquals(new BigDecimal("5000"), result.amount());
        assertEquals(PaymentMethod.UPI, result.paymentMethod());
        assertEquals(PaymentStatus.COMPLETED, result.paymentStatus());

        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());

        verify(reservationRepository, times(1))
                .findByIdForUpdate(1L);

        verify(paymentRepository, times(1))
                .save(any(Payment.class));

        verify(reservationRepository, times(1))
                .save(reservation);

        verifyNoMoreInteractions(paymentRepository, reservationRepository);
    }

    @Test
    public void createPaymentInvalidRequestTest() {

        PaymentRequestDto dto = null;

        assertThrows(InvalidCallException.class, () ->
                paymentService.createPayment(dto, "customer1"));

        verifyNoInteractions(paymentRepository, reservationRepository);
    }

    @Test
    public void createPaymentWrongCustomerTest() {

        Reservation reservation = getReservation();

        PaymentRequestDto dto = new PaymentRequestDto(
                1L,
                new BigDecimal("5000"),
                PaymentMethod.UPI
        );

        when(reservationRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(reservation));

        assertThrows(InvalidCallException.class, () ->
                paymentService.createPayment(dto, "customer2"));

        verify(reservationRepository, times(1))
                .findByIdForUpdate(1L);

        verifyNoMoreInteractions(reservationRepository);
        verifyNoInteractions(paymentRepository);
    }

    @Test
    public void createPaymentAmountMismatchTest() {

        Reservation reservation = getReservation();

        PaymentRequestDto dto = new PaymentRequestDto(
                1L,
                new BigDecimal("3000"),
                PaymentMethod.UPI
        );

        when(reservationRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(reservation));

        assertThrows(InvalidCallException.class, () ->
                paymentService.createPayment(dto, "customer1"));

        verify(reservationRepository, times(1))
                .findByIdForUpdate(1L);

        verifyNoMoreInteractions(reservationRepository);
        verifyNoInteractions(paymentRepository);
    }

    @Test
    public void getMyPaymentsTest() {

        PaymentResponseDto responseDto = new PaymentResponseDto(
                1L,
                1L,
                "Honda City",
                "customer1",
                new BigDecimal("5000"),
                PaymentMethod.UPI,
                PaymentStatus.COMPLETED,
                "TXN-123456",
                null,
                null,
                null
        );

        Page<PaymentResponseDto> paymentPage =
                new PageImpl<>(List.of(responseDto));

        when(paymentRepository.getPaymentsByCustomerUsername(
                eq("customer1"), any(PageRequest.class)))
                .thenReturn(paymentPage);

        PaymentFilterDto result =
                paymentService.getMyPayments("customer1", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.list().size());
        assertEquals("Honda City", result.list().get(0).carModel());
        assertEquals(1, result.paginationInfo().numRecordsInPage());

        verify(paymentRepository, times(1))
                .getPaymentsByCustomerUsername(
                        eq("customer1"), any(PageRequest.class));

        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    public void getMyPaymentsInvalidPaginationTest() {

        assertThrows(InvalidCallException.class, () ->
                paymentService.getMyPayments("customer1", -1, 10));

        verifyNoInteractions(paymentRepository);
    }

    @Test
    public void getCompletedPaymentsTest() {

        PaymentResponseDto responseDto = new PaymentResponseDto(
                1L,
                1L,
                "Honda City",
                "customer1",
                new BigDecimal("5000"),
                PaymentMethod.UPI,
                PaymentStatus.COMPLETED,
                "TXN-123456",
                null,
                null,
                null
        );

        Page<PaymentResponseDto> paymentPage =
                new PageImpl<>(List.of(responseDto));

        when(paymentRepository.filterPaymentsByStatus(
                eq(PaymentStatus.COMPLETED),
                eq(PaymentMethod.UPI),
                any(),
                any(),
                eq("%customer1%"),
                any(PageRequest.class)))
                .thenReturn(paymentPage);

        PaymentFilterDto result = paymentService.getCompletedPayments(
                PaymentMethod.UPI,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                " customer1 ",
                0,
                10
        );

        assertNotNull(result);
        assertEquals(1, result.list().size());
        assertEquals(PaymentStatus.COMPLETED,
                result.list().get(0).paymentStatus());

        verify(paymentRepository, times(1))
                .filterPaymentsByStatus(
                        eq(PaymentStatus.COMPLETED),
                        eq(PaymentMethod.UPI),
                        any(),
                        any(),
                        eq("%customer1%"),
                        any(PageRequest.class));

        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    public void getCompletedPaymentsInvalidDateRangeTest() {

        assertThrows(InvalidCallException.class, () ->
                paymentService.getCompletedPayments(
                        null,
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 1, 1),
                        null,
                        0,
                        10
                ));

        verifyNoInteractions(paymentRepository);
    }
}
