package genum.payment.controller;

import genum.shared.payment.domain.ProductRequest;
import genum.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Controller
@RequestMapping("api/internal/payment/paystack")
public class PaystackController {


    private final PaymentService paymentService;

    public PaystackController(@Qualifier("paystack") PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("initialize")
    @Async
    public Future<?> initializeTransaction(@RequestBody ProductRequest productRequest) {
        return CompletableFuture.completedFuture(paymentService.initializePayment(productRequest));
    }
    @GetMapping(value = "verify", params = {"reference","payment_id"})
    @Async
    public Future<?> verifyTransaction(@RequestParam("reference") String reference,
                                                     @RequestParam("payment_id") String paymentId){
        return CompletableFuture.completedFuture(paymentService.verifyPayment(reference, paymentId));
    }
}
