package barojob.server.domain.employer.controller;

import barojob.server.domain.match.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmployerController {
    private final MatchService matchService;

}
