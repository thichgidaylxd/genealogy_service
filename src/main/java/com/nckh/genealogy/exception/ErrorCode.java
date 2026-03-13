package com.nckh.genealogy.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ==================== COMMON ====================
    INTERNAL_SERVER_ERROR(500, "Lỗi hệ thống, vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST(400, "Dữ liệu đầu vào không hợp lệ", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND(404, "Không tìm thấy tài nguyên", HttpStatus.NOT_FOUND),
    UNAUTHORIZED(401, "Bạn chưa đăng nhập", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403, "Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN),
    CONFLICT(409, "Dữ liệu đã tồn tại", HttpStatus.CONFLICT),

    // ==================== AUTH ====================
    INVALID_CREDENTIALS(401, "Tên đăng nhập hoặc mật khẩu không đúng", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(401, "Phiên đăng nhập đã hết hạn", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(401, "Token không hợp lệ", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_INVALID(401, "Refresh token không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED(403, "Tài khoản đã bị vô hiệu hóa", HttpStatus.FORBIDDEN),

    // ==================== USER ====================
    USER_NOT_FOUND(404, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    USERNAME_ALREADY_EXISTS(409, "Tên đăng nhập đã tồn tại", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS(409, "Email đã được sử dụng", HttpStatus.CONFLICT),
    PHONE_ALREADY_EXISTS(409, "Số điện thoại đã được sử dụng", HttpStatus.CONFLICT),

    // ==================== TREE ====================
    TREE_NOT_FOUND(404, "Không tìm thấy gia phả", HttpStatus.NOT_FOUND),
    TREE_MEMBER_NOT_FOUND(404, "Không tìm thấy thành viên trong gia phả", HttpStatus.NOT_FOUND),
    TREE_MEMBER_ALREADY_EXISTS(409, "Người dùng đã là thành viên của gia phả", HttpStatus.CONFLICT),
    TREE_ACCESS_DENIED(403, "Bạn không có quyền truy cập gia phả này", HttpStatus.FORBIDDEN),
    TREE_INVITATION_NOT_FOUND(404, "Không tìm thấy lời mời", HttpStatus.NOT_FOUND),
    TREE_INVITATION_EXPIRED(400, "Lời mời đã hết hạn", HttpStatus.BAD_REQUEST),
    TREE_INVITATION_ALREADY_USED(400, "Lời mời đã được sử dụng", HttpStatus.BAD_REQUEST),
    TREE_SHARE_LINK_NOT_FOUND(404, "Không tìm thấy link chia sẻ", HttpStatus.NOT_FOUND),
    TREE_SHARE_LINK_EXPIRED(400, "Link chia sẻ đã hết hạn", HttpStatus.BAD_REQUEST),

    // ==================== PERSON ====================
    PERSON_NOT_FOUND(404, "Không tìm thấy nhân vật", HttpStatus.NOT_FOUND),
    PERSON_ALREADY_IN_TREE(409, "Nhân vật đã tồn tại trong gia phả", HttpStatus.CONFLICT),
    CITIZEN_ID_ALREADY_EXISTS(409, "Số CCCD đã tồn tại", HttpStatus.CONFLICT),

    // ==================== FAMILY ====================
    FAMILY_NOT_FOUND(404, "Không tìm thấy gia đình", HttpStatus.NOT_FOUND),
    FAMILY_ALREADY_EXISTS(409, "Quan hệ gia đình đã tồn tại", HttpStatus.CONFLICT),
    PERSON_ALREADY_A_CHILD(409, "Nhân vật đã là con trong gia đình này", HttpStatus.CONFLICT),

    // ==================== ADDRESS ====================
    ADDRESS_NOT_FOUND(404, "Không tìm thấy địa chỉ", HttpStatus.NOT_FOUND),
    ADDRESS_TYPE_NOT_FOUND(404, "Không tìm thấy loại địa chỉ", HttpStatus.NOT_FOUND),

    // ==================== EVENT ====================
    EVENT_NOT_FOUND(404, "Không tìm thấy sự kiện", HttpStatus.NOT_FOUND),
    EVENT_TYPE_NOT_FOUND(404, "Không tìm thấy loại sự kiện", HttpStatus.NOT_FOUND),
    ROLE_IN_EVENT_NOT_FOUND(404, "Không tìm thấy vai trò trong sự kiện", HttpStatus.NOT_FOUND),

    // ==================== MEDIA ====================
    MEDIA_FILE_NOT_FOUND(404, "Không tìm thấy tệp media", HttpStatus.NOT_FOUND),
    MEDIA_FILE_TYPE_NOT_FOUND(404, "Không tìm thấy loại tệp media", HttpStatus.NOT_FOUND),
    FILE_UPLOAD_FAILED(500, "Tải lên tệp thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_TYPE_NOT_SUPPORTED(400, "Định dạng tệp không được hỗ trợ", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED(400, "Kích thước tệp vượt quá giới hạn", HttpStatus.BAD_REQUEST),

    // ==================== FUND ====================
    FUND_NOT_FOUND(404, "Không tìm thấy quỹ", HttpStatus.NOT_FOUND),
    FUND_ALREADY_EXISTS(409, "Gia phả đã có quỹ", HttpStatus.CONFLICT),
    FUND_EVENT_NOT_FOUND(404, "Không tìm thấy quỹ sự kiện", HttpStatus.NOT_FOUND),
    FUND_TRANSACTION_NOT_FOUND(404, "Không tìm thấy giao dịch", HttpStatus.NOT_FOUND),
    FUND_INSUFFICIENT_BALANCE(400, "Số dư quỹ không đủ", HttpStatus.BAD_REQUEST),

    // ==================== NOTIFICATION ====================
    NOTIFICATION_NOT_FOUND(404, "Không tìm thấy thông báo", HttpStatus.NOT_FOUND),

    // ==================== ROLE ====================
    ROLE_NOT_FOUND(404, "Không tìm thấy vai trò", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}