package genum.data.payment.controller;

import genum.data.shared.payment.domain.PaymentResponse;
import genum.data.shared.payment.domain.ProductRequest;
import genum.data.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;



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
    public PaymentResponse initializeTransaction(@RequestBody ProductRequest productRequest) {
        return paymentService.initializePayment(productRequest);
    }
    @GetMapping(value = "verify", params = {"reference","payment_id"})
    public PaymentResponse verifyTransaction(@RequestParam("reference") String reference,
                                       @RequestParam("payment_id") String paymentId){
        return paymentService.verifyPayment(reference, paymentId);
    }
}
