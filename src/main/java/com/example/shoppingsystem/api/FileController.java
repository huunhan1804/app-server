package com.example.shoppingsystem.api;


import com.example.shoppingsystem.awsS3.S3BucketStorageService;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.responses.UploadFileResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
@Tag(name = "File")
public class FileController {
    private final S3BucketStorageService service;

    @Autowired
    public FileController(S3BucketStorageService service) {
        this.service = service;
    }

    @PostMapping(value ="/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<UploadFileResponse>> uploadFile(@RequestParam("fileName") String fileName,
                                                                      @RequestParam("file") MultipartFile file) {
        ApiResponse<UploadFileResponse> apiResponse = service.uploadFile(fileName, file);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

}

