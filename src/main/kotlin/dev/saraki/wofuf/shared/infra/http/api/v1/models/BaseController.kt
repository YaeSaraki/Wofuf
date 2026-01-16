package dev.saraki.wofuf.shared.infra.http.api.v1.models

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 22:09
 *   @description:
 */
@RestController
@RequestMapping("/api/v1")
public class BaseController {

    @GetMapping("/")
    public fun root(): ApiResponse<String> {
        return ApiResponse.success("Yo! QWQ");
    }
}
