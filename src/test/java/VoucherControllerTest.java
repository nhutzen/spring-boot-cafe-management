import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import vn.edu.fpt.cafemanagement.controllers.VoucherController;
import vn.edu.fpt.cafemanagement.entities.Voucher;
import vn.edu.fpt.cafemanagement.services.VoucherService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherControllerTest {

    @Mock
    private VoucherService voucherService;

    @InjectMocks
    private VoucherController voucherController;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private Model model;

    private Voucher voucher;

    @BeforeEach
    void setUp() {
        voucher = new Voucher();
        voucher.setVoucherId(0);
        voucher.setCode("VCH100");
        voucher.setVoucherName("Discount");
        voucher.setDiscountType("Percent");
        voucher.setQuantity(10);
        voucher.setMinOrderValue(0);
        voucher.setDiscountValue(10);
        voucher.setStartDate(LocalDate.of(2025, 10, 20));
        voucher.setEndDate(LocalDate.of(2025, 10, 25));
    }



    //  1. Empty required fields
    @Test
    void testSave_BlankCode() {
        voucher.setCode("");

        String result = voucherController.save(voucher, null, bindingResult, model);

        assertEquals("dashboard/vouchers/create", result);
        verify(model).addAttribute("message", "Error: Please fill all the fields!");
    }

    // 2
    @Test
    void testSave_BlankVoucherName() {
        voucher.setVoucherName("");

        String result = voucherController.save(voucher, null, bindingResult, model);

        assertEquals("dashboard/vouchers/create", result);
        verify(model).addAttribute("message", "Error: Please fill all the fields!");
    }

    // 3
    @Test
    void testSave_BlankDiscountType() {
        voucher.setDiscountType("");

        String result = voucherController.save(voucher, null, bindingResult, model);

        assertEquals("dashboard/vouchers/create", result);
        verify(model).addAttribute("message", "Error: Please fill all the fields!");
    }

    //  4. startDate or endDate null
    @Test
    void testSave_NullStartDate() {
        voucher.setStartDate(null);

        String result = voucherController.save(voucher, null, bindingResult, model);

        assertEquals("dashboard/vouchers/create", result);
        verify(model).addAttribute("message", "Error: Please select both start and end dates!");
    }

    // 5
    @Test
    void testSave_NullEndDate() {
        voucher.setEndDate(null);

        String result = voucherController.save(voucher, null, bindingResult, model);

        assertEquals("dashboard/vouchers/create", result);
        verify(model).addAttribute("message", "Error: Please select both start and end dates!");
    }

    //  6. startDate > endDate
    @Test
    void testSave_StartDateAfterEndDate() {
        voucher.setStartDate(LocalDate.of(2025, 10, 30));
        voucher.setEndDate(LocalDate.of(2025, 10, 20));

        String result = voucherController.save(voucher, null, bindingResult, model);

        assertEquals("dashboard/vouchers/create", result);
        verify(model).addAttribute("message", "Error: Start Date cannot be after End Date!");
    }

    //  7. bindingResult.hasErrors()
    @Test
    void testSave_WhenBindingResultHasErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = voucherController.save(voucher, null, bindingResult, model);

        assertEquals("dashboard/vouchers/create", result);
        verify(model).addAttribute("message", "Error: Invalid! Please try again.");
    }

    //  8. Invalid regex for code
    @Test
    void testSave_InvalidCodeRegex() {
        voucher.setCode("CODE@123");

        String result = voucherController.save(voucher, null, bindingResult, model);

        assertEquals("dashboard/vouchers/create", result);
        verify(model).addAttribute("message",
                "Error: Voucher Name or Voucher Code is invalid!. EX: VCH123 or DISCOUNT50%");
    }

    //  9. Invalid regex for vc name
    @Test
    void testSave_InvalidVoucherNameRegex() {
        voucher.setVoucherName("DISCOUNT@1");

        String result = voucherController.save(voucher, null, bindingResult, model);

        assertEquals("dashboard/vouchers/create", result);
        verify(model).addAttribute("message",
                "Error: Voucher Name or Voucher Code is invalid!. EX: VCH123 or DISCOUNT50%");
    }

    //  10. Quantity <= 0
    @Test
    void testSave_InvalidQuantity() {
        voucher.setQuantity(0);

        String result = voucherController.save(voucher, null, bindingResult, model);

        assertEquals("dashboard/vouchers/create", result);
        verify(model).addAttribute("message", "Error: Quantity must be more than 0!");
    }

    //  11. Min Order Value < 0
    @Test
    void testSave_InvalidMinOrderValue() {
        voucher.setMinOrderValue(-1);

        String result = voucherController.save(voucher, null, bindingResult, model);

        assertEquals("dashboard/vouchers/create", result);
        verify(model).addAttribute("message", "Error: Min order value must be more than or equal to 0!");
    }

    //  12. Min Order Value = 0
    @Test
    void testSave_BoundaryMinOrderValue() {
        voucher.setMinOrderValue(0);

        String result = voucherController.save(voucher, null, bindingResult, model);

        assertEquals("redirect:/dashboard/vouchers/list", result);
    }

    //  13. Discount Value <= 0
    @Test
    void testSave_InvalidDiscountValue() {
        voucher.setDiscountValue(0);

        String result = voucherController.save(voucher, null, bindingResult, model);

        assertEquals("dashboard/vouchers/create", result);
        verify(model).addAttribute("message", "Error: Discount value must be more than 0!");
    }

    //  14. Duplicate voucher
    @Test
    void testSave_DuplicateVoucherCode() {
        Voucher exist = new Voucher();
        exist.setCode("VCH100");

        when(voucherService.findAll()).thenReturn(List.of(exist));

        String result = voucherController.save(voucher, null, bindingResult, model);

        assertEquals("dashboard/vouchers/create", result);
        verify(model).addAttribute("message", "Error: Voucher Name or Code already exists!");
    }

    //  14. Duplicate voucher
    @Test
    void testSave_DuplicateVoucherName() {
        Voucher exist = new Voucher();
        exist.setVoucherName("Discount");

        when(voucherService.findAll()).thenReturn(List.of(exist));

        String result = voucherController.save(voucher, null, bindingResult, model);

        assertEquals("dashboard/vouchers/create", result);
        verify(model).addAttribute("message", "Error: Voucher Name or Code already exists!");
    }

    //  16. Success case
    @Test
    void testSave_Success() {
        voucher.setMinOrderValue(50000);

        when(voucherService.findAll()).thenReturn(List.of());

        String result = voucherController.save(voucher, null, bindingResult, model);

        assertEquals("redirect:/dashboard/vouchers/list", result);
    }
}
