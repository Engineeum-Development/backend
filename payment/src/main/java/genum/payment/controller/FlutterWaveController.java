package genum.payment.controller;

import genum.payment.service.PaymentService;
import genum.shared.payment.constants.PaymentStatus;
import genum.shared.payment.domain.PaymentResponse;
import genum.shared.payment.domain.ProductRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;


@RestController
@RequestMapping("api/payment/flutter_wave")
public class FlutterWaveController {

    private final PaymentService paymentService;

    public FlutterWaveController(@Qualifier("flutter_wave") PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("initialize")
    public PaymentResponse initializeTransaction(@RequestBody ProductRequest productRequest) {
        return paymentService.initializePayment(productRequest);
    }
    @GetMapping(value = "verify-callback")
    public PaymentResponse verifyTransaction(@RequestParam("status") String status,
                                       @RequestParam("tx_ref") String reference,
                                             @RequestParam("transaction_id") String transactionId){
        if (status.equals("successful")) {
            return paymentService.verifyPayment(reference, transactionId);
        } else {
            return new PaymentResponse(LocalDateTime.now().toString(),
                    PaymentStatus.FAILED, Map.of(
                    "message", "Payment failed, please try again",
                    "status", "failed"));
        }
    }
}
