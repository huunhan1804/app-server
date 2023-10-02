package com.example.shoppingsystem.constants;

public class ErrorCode {
    public static final int SUCCESS = 200; // Mã thành công
    public static final int BAD_REQUEST = 400; // Mã yêu cầu không hợp lệ
    public static final int UNAUTHORIZED = 401; // Mã không được ủy quyền
    public static final int FORBIDDEN = 403; // Mã truy cập bị từ chối
    public static final int NOT_FOUND = 404; // Mã không tìm thấy
    public static final int INTERNAL_SERVER_ERROR = 500; // Mã lỗi máy chủ nội bộ
    public static final int TOO_MANY_REQUEST = 429; //Too Many Requests
    public static final int ACCOUNT_ALREADY_EXISTS = 409;

}
