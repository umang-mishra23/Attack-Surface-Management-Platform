package com.sentinelasm.finding;

import com.sentinelasm.user.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/findings")
public class FindingController {
    private final FindingRepository findings;
    private final UserService userService;

    public FindingController(FindingRepository findings, UserService userService) {
        this.findings = findings;
        this.userService = userService;
    }

    @GetMapping
    public List<Finding> list() {
        return findings.findByOwnerOrderByDateFoundDesc(userService.currentUser());
    }
}
