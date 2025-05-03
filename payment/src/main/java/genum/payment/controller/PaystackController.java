package genum.payment.controller;

import genum.payment.domain.PaystackWebhook;
import genum.payment.service.PaymentService;
import genum.shared.payment.domain.PaymentResponse;
import genum.shared.payment.domain.ProductRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@Slf4j
@RequestMapping("api/payment/paystack")
public class PaystackController {


    private final PaymentService paymentService;

    public PaystackController(@Qualifier("paystack") PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initialize")
    public PaymentResponse initializeTransaction(@RequestBody ProductRequest productRequest) {
        var paymentResponse = paymentService.initializePayment(productRequest);
        log.info("Payment response: {}", paymentResponse);
        return paymentResponse;
    }
    @GetMapping(value = "/verify")
    public PaymentResponse verifyTransaction(@RequestParam("txref") String reference){
        return paymentService.verifyPayment(reference);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody PaystackWebhook webHook) {
        paymentService.handleWebHook(webHook);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
