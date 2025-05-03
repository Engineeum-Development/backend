package genum.payment.controller;

import genum.payment.service.PaymentService;
import genum.shared.payment.domain.PaymentResponse;
import genum.shared.payment.domain.ProductRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/payment/flutter_wave")
public class FlutterWaveController {

    private final PaymentService paymentService;

    public FlutterWaveController(@Qualifier("flutter_wave") PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initialize")
    public PaymentResponse initializeTransaction(@RequestBody ProductRequest productRequest) {
        return paymentService.initializePayment(productRequest);
    }

    @GetMapping(value = "/verify")
    public PaymentResponse verifyTransaction(@RequestParam("tx_ref") String referenceRef) {
        return paymentService.verifyPayment(referenceRef);
    }
}
