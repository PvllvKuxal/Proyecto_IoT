package com.example.Proyecto_IoT.controller;
import com.example.Proyecto_IoT.dto.user.UserDevicesDTO;
import com.example.Proyecto_IoT.service.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService UserService;

    @GetMapping("/{id}/devices")
    public ResponseEntity<?> userDevices(@PathVariable Long id) {
        UserDevicesDTO response = UserService.getUserDevices(id);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

}