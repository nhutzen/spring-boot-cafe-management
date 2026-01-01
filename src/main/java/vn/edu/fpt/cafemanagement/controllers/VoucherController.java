package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cafemanagement.entities.Voucher;
import vn.edu.fpt.cafemanagement.services.VoucherService;
import vn.edu.fpt.cafemanagement.util.SignUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Controller
public class VoucherController {
    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping(value = {"/dashboard/vouchers", "/dashboard/vouchers/list"})
    public String getVouchers(Model model, @RequestParam(name = "keyword", required = false) String keyword) {
        List<Voucher> allVouchers = voucherService.findAll();
        List<Voucher> activeVouchers = voucherService.findAll().stream().filter(Voucher::isActive).collect(Collectors.toList());
        Stream<Voucher> activeVoucherStream = allVouchers.stream().filter(Voucher::isActive);
        if (keyword != null && !keyword.isEmpty()) {
            String lowerCaseKeyword = keyword.toLowerCase().trim();
            activeVoucherStream = activeVoucherStream.filter(v -> v.getVoucherName().toLowerCase().contains(lowerCaseKeyword));
        }
        activeVouchers = activeVoucherStream.collect(Collectors.toList());
        activeVouchers.forEach(v -> v.setSignature(SignUtil.sign(String.valueOf(v.getVoucherId())))); //lambda expression
        model.addAttribute("vouchers", activeVouchers);
        model.addAttribute("keyword", keyword);
        return "/dashboard/vouchers/list";
    }

    @RequestMapping(value = "/dashboard/vouchers/create")
    public String createVoucher(Model model) {
        model.addAttribute("voucher", new Voucher());
        return "dashboard/vouchers/create";
    }

    @RequestMapping(value = "/dashboard/vouchers/edit/{id}")
    public String editVoucher(Model model, @PathVariable("id") int id, Voucher voucher) {
        voucher.setVoucherId(id);
        String signature = SignUtil.sign(String.valueOf(voucher.getVoucherId()));
        model.addAttribute("voucher", voucherService.findById(id));
        model.addAttribute("sig", signature);
        return "dashboard/vouchers/edit";
    }

    @RequestMapping(value = "dashboard/vouchers/save", method = RequestMethod.POST)
    public String save(@Validated @ModelAttribute(name = "voucher") Voucher voucher, @RequestParam(name = "sig", required = false) String signature, BindingResult bindingResult, Model model) {
//        exception data
        if (bindingResult.hasErrors()) {
            model.addAttribute("message", "Error: Invalid! Please try again.");
            if (voucher.getVoucherId() == 0) {
                //  create
                return "dashboard/vouchers/create";
            } else {
                //  edit
                model.addAttribute("sig", signature);
                return "dashboard/vouchers/edit";
            }
        }
//        validate blank
        if (voucher.getCode().isBlank() || voucher.getVoucherName().isBlank() || voucher.getDiscountType().isBlank()) {
            model.addAttribute("message", "Error: Please fill all the fields!");
            if (voucher.getVoucherId() == 0) {
                //  create
                return "dashboard/vouchers/create";
            } else {
                //  edit
                model.addAttribute("sig", signature);
                return "dashboard/vouchers/edit";
            }
        }

        //check quantity > 0
        if (voucher.getQuantity() <= 0) {
            model.addAttribute("message", "Error: Quantity must be more than 0!");
            if (voucher.getVoucherId() == 0) {
                //  create
                return "dashboard/vouchers/create";
            } else {
                //  edit
                model.addAttribute("sig", signature);
                return "dashboard/vouchers/edit";
            }
        }

        //check min order value >= 0
        if (voucher.getMinOrderValue() < 0) {
            model.addAttribute("message", "Error: Min order value must be more than or equal to 0!");
            if (voucher.getVoucherId() == 0) {
                //  create
                return "dashboard/vouchers/create";
            } else {
                //  edit
                model.addAttribute("sig", signature);
                return "dashboard/vouchers/edit";
            }
        }

        //check discount value > 0
        if (voucher.getDiscountValue() <= 0) {
            model.addAttribute("message", "Error: Discount value must be more than 0!");
            if (voucher.getVoucherId() == 0) {
                //  create
                return "dashboard/vouchers/create";
            } else {
                //  edit
                model.addAttribute("sig", signature);
                return "dashboard/vouchers/edit";
            }
        }


//        validate voucher name and voucher code
        if (!voucher.getCode().matches("[A-Za-z0-9%]+") || !voucher.getVoucherName().matches("[\\p{L}0-9%\\s]+")) {
            model.addAttribute("message", "Error: Voucher Name or Voucher Code is invalid!. EX: VCH123 or DISCOUNT50%");
            if (voucher.getVoucherId() == 0) {
                //  create
                return "dashboard/vouchers/create";
            } else {
                //  edit
                model.addAttribute("sig", signature);
                return "dashboard/vouchers/edit";
            }
        }
//       Validate Date
        if (voucher.getStartDate() == null || voucher.getEndDate() == null) {
            model.addAttribute("message", "Error: Please select both start and end dates!");
            return voucher.getVoucherId() == 0 ? "dashboard/vouchers/create" : "edit";
        }
        if (voucher.getStartDate().isAfter(voucher.getEndDate())) {
            model.addAttribute("message", "Error: Start Date cannot be after End Date!");
            if (voucher.getVoucherId() == 0) {
                //  create
                return "dashboard/vouchers/create";
            } else {
                //  edit
                model.addAttribute("sig", signature);
                return "dashboard/vouchers/edit";
            }
        }
        if (voucher.getEndDate().isBefore(voucher.getStartDate())) {
            model.addAttribute("message", "Error: End Date cannot be before Start Date!");
            if (voucher.getVoucherId() == 0) {
                //  create
                return "dashboard/vouchers/create";
            } else {
                //  edit
                model.addAttribute("sig", signature);
                return "dashboard/vouchers/edit";
            }
        }
//        check duplicate
        System.out.println("create " + voucher.getVoucherId());
        if (voucher.getVoucherId() == 0) {
            for (Voucher list : voucherService.findAll()) {
                if (voucher.getVoucherName().equals(list.getVoucherName()) || voucher.getCode().equals(list.getCode())) {
                    model.addAttribute("message", "Error: Voucher Name or Code already exists!");
                    return "dashboard/vouchers/create";
                }
            }
        }
        if (voucher.getVoucherId() != 0) {
            boolean valid = SignUtil.verify(String.valueOf(voucher.getVoucherId()), signature);
            if (!valid) {
                model.addAttribute("message", "⚠️DO NOT F12!!!!!!, YEAH I'M TELLING U");
                return "/dashboard/vouchers/edit";
            }
        }

        voucher.setActive(true);
        voucherService.save(voucher);
        return "redirect:/dashboard/vouchers/list";
    }

    @RequestMapping(value = "/dashboard/vouchers/remove", method = RequestMethod.POST)
    public String remove(@ModelAttribute(name = "voucher") Voucher voucher, @RequestParam("voucherId") int id, @RequestParam("sig") String sig, Model model) {
        if (!SignUtil.verify(String.valueOf(id), sig)) {
            model.addAttribute("error", "⚠️DO NOT F12!!!!!!, YEAH I'M TELLING U");
            return "/dashboard/vouchers/list";
        }

        voucher = voucherService.findById(id);
        if (voucher != null) {
            voucher.setActive(false);
            voucherService.save(voucher);
        }

        return "redirect:/dashboard/vouchers/list";
    }

    @RequestMapping(value = "/dashboard/vouchers/deleted-list")
    public String trashVoucher(Model model, @RequestParam(name = "keyword", required = false) String keyword) {
        List<Voucher> inactiveVouchers = voucherService.getNoActiveVouchers();
        Stream<Voucher> voucherStream = inactiveVouchers.stream();
        if (keyword != null && !keyword.isEmpty()) {
            String lowerCaseKeyword = keyword.toLowerCase().trim();
            voucherStream = voucherStream.filter(v -> v.getVoucherName().toLowerCase().contains(lowerCaseKeyword));
        }
        List<Voucher> finalVouchers = voucherStream.collect(Collectors.toList());
        model.addAttribute("vouchers", finalVouchers);
        model.addAttribute("keyword", keyword);
        return "dashboard/vouchers/deleted-list";
    }

    @RequestMapping(value = "/dashboard/vouchers/restore")
    public String restore(Model model, Voucher voucher, @RequestParam(name = "voucherId") int id) {
        System.out.println("HELLLLLL");
        voucher = voucherService.findById(id);
        voucher.setActive(true);
        voucherService.save(voucher);
        model.addAttribute("vouchers", voucherService.getNoActiveVouchers());
        return "dashboard/vouchers/deleted-list";
    }
}
