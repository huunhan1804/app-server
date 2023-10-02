package com.example.shoppingsystem.awsS3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.shoppingsystem.constants.ErrorCode;
import com.example.shoppingsystem.constants.LogMessage;
import com.example.shoppingsystem.constants.Message;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.responses.UploadFileResponse;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

@Service
public class S3BucketStorageService {

    @Autowired
    private AmazonS3 amazonS3Client;

    @Value("${application.bucket.name}")
    private String bucketName;

    public ApiResponse<UploadFileResponse> uploadFile(String keyName, MultipartFile file) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            PutObjectRequest request = new PutObjectRequest(bucketName, keyName, file.getInputStream(), metadata);
            request.setCannedAcl(CannedAccessControlList.PublicRead);
            amazonS3Client.putObject(request);
            UploadFileResponse uploadFileResponse = UploadFileResponse.builder()
                    .message_upload("File uploaded: " + keyName)
                    .file_url("https://webdata.hn.ss-website.bfcplatform.vn/" + bucketName + "/" + keyName)
                    .build();
            return ApiResponse.<UploadFileResponse>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.SUCCESS)
                    .data(uploadFileResponse)
                    .timestamp(new Date())
                    .build();
        } catch (IOException ioe) {
            return ApiResponse.<UploadFileResponse>builder()
                    .status(ErrorCode.INTERNAL_SERVER_ERROR)
                    .message("IOException: " + ioe.getMessage())
                    .timestamp(new Date())
                    .build();
        } catch (AmazonServiceException serviceException) {
            return ApiResponse.<UploadFileResponse>builder()
                    .status(ErrorCode.INTERNAL_SERVER_ERROR)
                    .message("AmazonServiceException: " + serviceException.getMessage())
                    .timestamp(new Date())
                    .build();
        } catch (AmazonClientException clientException) {
            return ApiResponse.<UploadFileResponse>builder()
                    .status(ErrorCode.INTERNAL_SERVER_ERROR)
                    .message("AmazonClientException Message: " + clientException.getMessage())
                    .timestamp(new Date())
                    .build();
        }
    }

}
