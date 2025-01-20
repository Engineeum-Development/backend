package genum.payment.controller;

import genum.payment.domain.ProductRequest;
import genum.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * These endpoints will only be called from within the applications
 */
@Controller
@RequestMapping("api/internal/payment/flutter_wave")
public class FlutterWaveController {

    private final PaymentService paymentService;

    public FlutterWaveController(@Qualifier("flutter_wave") PaymentService paymentService) {
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
