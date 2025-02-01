package genum.payment.controller;

import genum.shared.payment.domain.PaymentResponse;
import genum.shared.payment.domain.ProductRequest;
import genum.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@RestController
@RequestMapping("api/payment/paystack")
public class PaystackController {


    private final PaymentService paymentService;

    public PaystackController(@Qualifier("paystack") PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("initialize")
    public PaymentResponse initializeTransaction(@RequestBody ProductRequest productRequest) {
        return paymentService.initializePayment(productRequest);
    }
    @GetMapping(value = "verify")
    public PaymentResponse verifyTransaction(@RequestParam("reference") String reference,
                                                     @RequestParam("payment_id") String paymentId){
        return paymentService.verifyPayment(reference, paymentId);
    }
}
