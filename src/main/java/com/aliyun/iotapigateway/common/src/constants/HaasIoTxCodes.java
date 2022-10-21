package com.aliyun.iotx.haas.tdserver.common.constants;

import com.aliyun.iotx.common.base.code.IoTxCode;

public class HaasIoTxCodes {

    /* 公共错误码 (100) */
    public static IoTxCode ERROR_DATABASE_ACCESS = new IoTxCode(100001, "Database access error", "数据库读写错误");
    public static IoTxCode ERROR_SERVER_INTERNAL = new IoTxCode(100002, "Server internal error", "服务端内部");
    public static IoTxCode ERROR_AES_UTIL_ENCRYPT = new IoTxCode(100003, "AES Util Encrypt Error", "AES加密错误");
    public static IoTxCode ERROR_SERVER_EXTERNAL = new IoTxCode(100004, "Extern server error", "三方服务端错误");

    /* UserService 错误码 (101) */
    /* login (1011) */
    public static IoTxCode ERROR_LOGIN_PLATFORM_EMPTY = new IoTxCode(101101, "User login error: Parameter 'platform' is required", "用户登录错误：参数'platform'不可为空");
    public static IoTxCode ERROR_LOGIN_PLATFORM_TOO_LONG = new IoTxCode(101102, "User login error: Parameter 'platform' is too long (32 characters max)", "用户登录错误：platform参数超长，最大允许长度为32字节");
    public static IoTxCode ERROR_LOGIN_USER_ID_EMPTY = new IoTxCode(101103, "User login error: Parameter 'userId' is required", "用户登录错误：参数'userId'不可为空");
    public static IoTxCode ERROR_LOGIN_USER_ID_TOO_LONG = new IoTxCode(101104, "User login error: Parameter 'userId' is too long (64 characters max)", "用户登录错误：userId参数超长，最大允许长度为64字节");
    public static IoTxCode ERROR_LOGIN_USER_ID_COLLISION = new IoTxCode(101105, "User login error: HaaS user id collision", "用户登录错误：HaaS平台用户ID发生碰撞");
    public static IoTxCode ERROR_USER_NEED_LOGIN = new IoTxCode(101106, "User login error: This user need login first", "用户登录错误：该用户需要先登陆");
    /* info (1012) */
    public static IoTxCode ERROR_INFO_HAAS_USER_ID_EMPTY = new IoTxCode(101201, "Get user info error: Parameter 'haasUserId' is required", "获取用户信息失败：haasUserId参数不可为空");
    public static IoTxCode ERROR_INFO_NOT_FOUND = new IoTxCode(101202, "Get user info error: User not found", "获取用户信息失败：HaaS平台未找到此用户");
    /* signAgreement (1013) */
    public static IoTxCode ERROR_AGREEMENT_SIGN_USER_NOT_EXIST = new IoTxCode(101301, "The specified haasUserId does not exist", "指定的 haasUserId 不存在");
    /* mobile (1014) */
    public static IoTxCode ERROR_MOBILE_USER_NOT_EXIST = new IoTxCode(101401, "The specified haasUserId does not exist", "指定的 haasUserId 不存在");
    public static IoTxCode ERROR_MOBILE_HAAS_USER_ID_EMPTY = new IoTxCode(101402, "Upgrade mobile error: Parameter 'haasUserId' is required", "更新手机号码失败：参数'haasUserId'不可为空");
    public static IoTxCode ERROR_MOBILE_MOBILE_EMPTY = new IoTxCode(101403, "Upgrade mobile error: Parameter 'mobile' is required", "更新手机号码失败：参数'mobile'不可为空");
    public static IoTxCode ERROR_MOBILE_VERIFICATION_CODE_EMPTY = new IoTxCode(101404, "Upgrade mobile error: Parameter 'verificationCode' is required", "更新手机号码失败：参数'verificationCode'不可为空");
    public static IoTxCode ERROR_MOBILE_NUMBER_FORMAT_ERROR = new IoTxCode(101405, "Upgrade mobile error: Wrong format of mobile number", "更新手机号码失败：手机号码格式错误");
    public static IoTxCode ERROR_MOBILE_GET_VERIFICATION_CODE_LATER = new IoTxCode(101406, "Get Verification code error: Get Verification code frequently", "获取短信验证码失败：获取验证码太频繁，请稍后在试");
    public static IoTxCode ERROR_MOBILE_VERIFICATION_CODE_ERROR = new IoTxCode(101407, "Upgrade mobile error: Wrong Verification code", "更新手机号码失败：验证码错误");
    public static IoTxCode ERROR_MOBILE_VERIFICATION_CODE_EXPIRE = new IoTxCode(101408, "Upgrade mobile error: Verification code expired", "更新手机号码失败：验证码过期或不存在");
    public static IoTxCode ERROR_MOBILE_NUMBER_NOT_EXIST = new IoTxCode(101409, "Upgrade mobile error: Mobile number is not exist", "更新手机号码失败：手机号码不存在");
    /* setUserSmsNotification (1015) */
    public static IoTxCode ERROR_SMS_NOTIFICATION_USER_NOT_EXIST = new IoTxCode(101501, "The specified haasUserId does not exist", "指定的 haasUserId 不存在");
    public static IoTxCode ERROR_SMS_NOTIFICATION_HAAS_USER_ID_EMPTY = new IoTxCode(101502, "Set sms Notification error: Parameter 'haasUserId' is required", "更新短信预警设置失败：参数'haasUserId'不可为空");
    public static IoTxCode ERROR_SMS_NOTIFICATION_IS_ENABLE_EMPTY = new IoTxCode(101503, "Set sms Notification error: Parameter 'isEnable' is required", "更新短信预警设置失败：参数'isEnable'不可为空");
    
    /* DeviceBindService 错误码 (102) */
    /* auth (1021) */
    public static IoTxCode ERROR_AUTH_HAAS_USER_ID_EMPTY = new IoTxCode(102101, "Auth error: Parameter 'haasUserId' is required", "设备鉴权失败：参数'haasUserId'不可为空");
    public static IoTxCode ERROR_AUTH_INVALID_USER = new IoTxCode(102102, "Auth error: HaaS user not found", "设备鉴权失败：HaaS用户不存在");
    public static IoTxCode ERROR_AUTH_PK_EMPTY = new IoTxCode(102103, "Auth error: Param 'productKey' is required'", "设备鉴权失败：参数'productKey'不可为空");
    public static IoTxCode ERROR_AUTH_DN_EMPTY = new IoTxCode(102104, "Auth error: Param 'deviceName' is required'", "设备鉴权失败：参数'deviceName'不可为空");
    public static IoTxCode ERROR_CHECK_CODE_EMPTY = new IoTxCode(102105, "Auth error: Param 'checkCode' is required'", "设备鉴权失败：参数'checkCode'不可为空");
    public static IoTxCode ERROR_AUTH_DEVICE_NOT_FOUND = new IoTxCode(102106, "Auth error: Device not found", "设备鉴权失败：LP接入平台中未找到此设备");
    public static IoTxCode ERROR_INVALID_CHECK_CODE = new IoTxCode(102107, "Auth error: Check code verification failed", "设备鉴权失败：绑定二维码校验信息验证失败");
    public static IoTxCode ERROR_AUTH_DEVICE_ALREADY_BOUND = new IoTxCode(102108, "Auth error: Device is already bound", "设备鉴权失败：此设备已被其他用户绑定");
    public static IoTxCode ERROR_AUTH_DEVICE_ALREADY_BOUND_CURRENT_USER = new IoTxCode(102109, "Auth error: This device is already bound to current user", "设备鉴权失败：您已绑定此设备，不可重复绑定");
    /* bind (1022) */
    public static IoTxCode ERROR_BIND_REQUEST_ID_EMPTY = new IoTxCode(102201, "Bind error: Parameter 'requestId' is required", "设备绑定失败：参数'requestId'不可为空");
    public static IoTxCode ERROR_BIND_R2_EMPTY = new IoTxCode(102202, "Bind error: Parameter 'r2' is required", "设备绑定失败：参数'r2'不可为空");
    public static IoTxCode ERROR_BIND_SIGN_EMPTY = new IoTxCode(102203, "Bind error: Parameter 'sign' is required", "设备绑定失败：参数'sign'不可为空");
    public static IoTxCode ERROR_BIND_R2_INVALID = new IoTxCode(102204, "Bind error: Invalid R2 code", "设备绑定失败：R2码不合法，R2不允许与R1相同");
    public static IoTxCode ERROR_BIND_UNAUTHORIZED_REQUEST = new IoTxCode(102205, "Bind error: Unauthorized device bind request", "设备绑定失败：未授权的绑定请求");
    public static IoTxCode ERROR_BIND_DEVICE_VERIFY = new IoTxCode(102206, "Bind error: Device verification error", "设备绑定失败：设备校验失败");
    public static IoTxCode ERROR_BIND_DEVICE_NOT_FOUND = new IoTxCode(102207, "Bind error: Device not found", "设备绑定失败：设备三元组未录入");
    /* unbind (1023) */
    public static IoTxCode ERROR_UNBIND_PK_EMPTY = new IoTxCode(102301, "Unbind error: Parameter 'productKey' is required", "设备解绑失败：参数'productKey'不可为空");
    public static IoTxCode ERROR_UNBIND_DN_EMPTY = new IoTxCode(102302, "Unbind error: Parameter 'deviceName' is required", "设备解绑失败：参数'deviceName'不可为空");
    public static IoTxCode ERROR_UNBIND_USER_EMPTY = new IoTxCode(102303, "Unbind error: Parameter 'haasUserId' is required", "设备解绑失败：参数'haasUserId'不可为空");
    public static IoTxCode ERROR_UNBIND_K1_EMPTY = new IoTxCode(102304, "Unbind error: Parameter 'k1' is required", "设备解绑失败：参数'k1'不可为空");
    public static IoTxCode ERROR_UNBIND_INVALID_USER = new IoTxCode(102305, "Unbind error: HaaS user not exists", "设备解绑失败：指定的用户不存在");
    public static IoTxCode ERROR_UNBIND_INVALID_DEVICE = new IoTxCode(102306, "Unbind error: Device not exists", "设备解绑失败：指定的设备不存在");
    public static IoTxCode ERROR_UNBIND_DEVICE_VERIFY = new IoTxCode(102307, "Unbind error: Device K1 code verification error", "设备解绑失败：设备K1密钥不匹配");
    public static IoTxCode ERROR_UNBIND_NOT_BIND = new IoTxCode(102308, "Unbind error: This device is not bound to this user", "设备解绑失败：此设备未与该用户绑定");

    /* DeviceShareService 错误码 (103) */
    /* shareQR (1031) */
    public static IoTxCode ERROR_SHARE_QR_HAAS_USER_ID_EMPTY = new IoTxCode(103101, "Share QR error: Parameter 'haasUserId' is required", "生成设备分享QR失败：参数'haasUserId'不可为空");
    public static IoTxCode ERROR_SHARE_QR_INVALID_USER = new IoTxCode(103102, "Share QR error: HaaS user not found", "生成设备分享QR失败：HaaS用户不存在");
    public static IoTxCode ERROR_SHARE_QR_PK_EMPTY = new IoTxCode(103103, "Share QR error: Param 'productKey' is required", "生成设备分享QR失败：参数'productKey'不可为空");
    public static IoTxCode ERROR_SHARE_QR_DN_EMPTY = new IoTxCode(103104, "Share QR error: Param 'deviceName' is required", "生成设备分享QR失败：参数'deviceName'不可为空");
    public static IoTxCode ERROR_SHARE_DN_NOT_BIND = new IoTxCode(103105, "Share QR error: This device is not bound to this user", "生成设备分享QR失败：此设备未与该用户绑定");
    /* share (1032) */
    public static IoTxCode ERROR_SHARE_HAAS_USER_ID_EMPTY = new IoTxCode(103201, "Share error: Parameter 'haasUserId' is required", "设备分享失败：参数'haasUserId'不可为空");
    public static IoTxCode ERROR_SHARE_INVALID_USER = new IoTxCode(103202, "Share error: HaaS user not found", "设备分享失败：HaaS用户不存在");
    public static IoTxCode ERROR_SHARE_PK_EMPTY = new IoTxCode(103203, "Share error: Param 'productKey' is required'", "设备分享失败：参数'productKey'不可为空");
    public static IoTxCode ERROR_SHARE_DN_EMPTY = new IoTxCode(103204, "Share error: Param 'deviceName' is required'", "设备分享失败：参数'deviceName'不可为空");
    public static IoTxCode ERROR_SHARE_TOKEN_EMPTY = new IoTxCode(103205, "Share error: Param 'token' is required'", "设备分享失败：参数'token'不可为空");
    public static IoTxCode ERROR_SHARE_INVALID_TOKEN = new IoTxCode(103206, "Share error: Param 'token' is invalid'", "设备分享失败：参数'token'无效");
    public static IoTxCode ERROR_SHARE_TOKEN_TIMEOUT = new IoTxCode(103207, "Share error: Token is Time out", "设备分享失败：参数'token'过期");
    public static IoTxCode ERROR_SHARE_DN_UNAUTHORIZED = new IoTxCode(103208, "Share error: Param 'deviceName' is unauthoried", "设备分享失败：参数'deviceName'未授权");
    public static IoTxCode ERROR_SHARE_USER_SELF = new IoTxCode(103209, "Share error: HaaS user is the same id'", "设备分享失败：被授权用户与授权用户ID相同");
    public static IoTxCode ERROR_SHARE_EXIST = new IoTxCode(103210, "Share error: Share Device is already bind", "设备分享失败：分享绑定关系已存在");
    /* unshare (1033) */
    public static IoTxCode ERROR_UNSHARE_HAAS_USER_ID_EMPTY = new IoTxCode(103301, "Unshare error: Parameter 'haasUserId' is required", "设备撤销分享失败：参数'haasUserId'不可为空");
    public static IoTxCode ERROR_UNSHARE_AUTHORIZEE_HAAS_USER_ID_EMPTY = new IoTxCode(103301, "Unshare error: Parameter 'authorizeeHaasUserId' is required", "设备撤销分享失败：参数'authorizeeHaasUserId'不可为空");
    public static IoTxCode ERROR_UNSHARE_INVALID_USER = new IoTxCode(103302, "Unshare error: HaaS user not found", "设备撤销分享失败：HaaS用户不存在");
    public static IoTxCode ERROR_UNSHARE_PK_EMPTY = new IoTxCode(103303, "Unshare error: Param 'productKey' is required'", "设备撤销分享失败：参数'productKey'不可为空");
    public static IoTxCode ERROR_UNSHARE_DN_EMPTY = new IoTxCode(103304, "Unshare error: Param 'deviceName' is required'", "设备撤销分享失败：参数'deviceName'不可为空");
    public static IoTxCode ERROR_UNSHARE_TOKEN_EMPTY = new IoTxCode(103305, "Unshare error: Param 'token' is required'", "设备撤销分享失败：参数'token'不可为空");
    public static IoTxCode ERROR_UNSHARE_INVALID_TOKEN = new IoTxCode(103306, "Unshare error: Param 'token' is invalid'", "设备撤销分享失败：参数'token'无效");
    public static IoTxCode ERROR_UNSHARE_TOKEN_TIMEOUT = new IoTxCode(103307, "Unshare error: Token is Time out", "设备撤销分享失败：参数'token'过期");
    public static IoTxCode ERROR_UNSHARE_DN_UNAUTHORIZED = new IoTxCode(103308, "Unshare error: Param 'deviceName' is unauthoried", "设备撤销分享失败：参数'deviceName'未授权");
    public static IoTxCode ERROR_UNSHARE_USER_SELF = new IoTxCode(103309, "Unshare error: HaaS user is the same id'", "设备撤销分享失败：被授权用户与授权用户ID相同");
    public static IoTxCode ERROR_UNSHARE_NOT_EXIST = new IoTxCode(103310, "Unshare error: Share Device is not bind", "设备撤销分享失败：解除分享绑定关系不存在");
    /* getShareDevice (1034) */
    public static IoTxCode ERROR_GET_SHARE_HAAS_USER_ID_EMPTY = new IoTxCode(103401, "Get Share Device error: Parameter 'haasUserId' is required", "获取分享设备失败：参数'haasUserId'不可为空");
    public static IoTxCode ERROR_GET_SHARE_INVALID_USER = new IoTxCode(103402, "Get Share Device error: HaaS user not found", "获取分享设备失败：HaaS用户不存在");
    public static IoTxCode ERROR_GET_SHARE_PK_EMPTY = new IoTxCode(103403, "Get Share Device error: Param 'productKey' is required", "获取分享设备失败：参数'productKey'不可为空");
    public static IoTxCode ERROR_GET_SHARE_DN_EMPTY = new IoTxCode(103404, "Get Share Device error: Param 'deviceName' is required", "获取分享设备失败：参数'deviceName'不可为空");
    public static IoTxCode ERROR_GET_SHARE_DN_NOT_BIND = new IoTxCode(103405, "Get Share Device error: This device is not bound to this user", "获取分享设备失败：此设备未与该用户绑定");
    public static IoTxCode ERROR_GET_SHARE_NOT_EXIST = new IoTxCode(103406, "Get Share Device error: Device is not exist", "获取分享设备失败：该用户不存在已分享设备");

    /* DeviceQRCodeService 错误码 (104) */
    /* deviceQRCode (1041) */
    public static IoTxCode ERROR_DEVICE_QR_HAAS_USER_ID_EMPTY = new IoTxCode(104101, "Device QR error: Parameter 'haasUserId' is required", "设备二维码获取失败：参数'haasUserId'不可为空");
    public static IoTxCode ERROR_DEVICE_QR_INVALID_USER = new IoTxCode(104102, "Device QR error: HaaS user not found", "设备二维码获取失败：HaaS用户不存在");
    public static IoTxCode ERROR_DEVICE_QR_PK_EMPTY = new IoTxCode(104103, "Device QR error: Param 'productKey' is required", "设备二维码获取失败：参数'productKey'不可为空");
    public static IoTxCode ERROR_DEVICE_QR_DN_EMPTY = new IoTxCode(104104, "Device QR error: Param 'deviceName' is required", "设备二维码获取失败：参数'deviceName'不可为空");
    public static IoTxCode ERROR_DEVICE_QR_UNAUTHORIZED = new IoTxCode(104105, "Device QR error: This device is not bound to this user", "设备二维码获取失败：此设备未与该用户绑定");

    /* DeviceManageService 错误码 (105) */
    /* queryDeviceProperties (1051) */
    public static IoTxCode ERROR_QUERY_DEVICE_PROPERTIES_HAAS_USER_ID_EMPTY = new IoTxCode(105101, "Query Device Properties error: Parameter 'haasUserId' is required", "查询设备快照失败：参数'haasUserId'不可为空");
    public static IoTxCode ERROR_QUERY_DEVICE_PROPERTIES_INVALID_USER = new IoTxCode(105102, "Query Device Properties error: HaaS user not found", "查询设备快照失败：HaaS用户不存在");
    public static IoTxCode ERROR_QUERY_DEVICE_PROPERTIES_PK_EMPTY = new IoTxCode(105103, "Query Device Properties error: Param 'productKey' is required'", "查询设备快照失败：参数'productKey'不可为空");
    public static IoTxCode ERROR_QUERY_DEVICE_PROPERTIES_DN_EMPTY = new IoTxCode(105104, "Query Device Properties error: Param 'deviceName' is required'", "查询设备快照失败：参数'deviceName'不可为空");
    public static IoTxCode ERROR_QUERY_DEVICE_PROPERTIES_IDENTIFIER_BLANK = new IoTxCode(105105, "Query Device Properties error: Device Identifier is blank'", "查询设备快照失败：设备Identifier为空");
    public static IoTxCode ERROR_QUERY_DEVICE_PROPERTIES_NO_STATUS = new IoTxCode(105106, "Query Device Properties error: Device status not found", "查询设备快照失败：LP接入平台获取此设备状态失败");
    public static IoTxCode ERROR_QUERY_DEVICE_PROPERTIES_NOT_BIND = new IoTxCode(105107, "Query Device Properties error: Device is not bound to this user", "查询设备快照失败：设备未绑定该用户");
    public static IoTxCode ERROR_QUERY_DEVICE_PROPERTIES_ALREADY_BOUND = new IoTxCode(105108, "Query Device Properties error: Device has not bound", "查询设备快照失败：此设备已被其他用户绑定");
    public static IoTxCode ERROR_QUERY_DEVICE_PROPERTIES_ALREADY_BOUND_CURRENT_USER = new IoTxCode(105109, "Query Device Properties error: This device is already bound to current user", "查询设备快照失败：您已绑定此设备，不可重复绑定");

    /* setDeviceProperties (1052) */
    public static IoTxCode ERROR_SET_DEVICE_PROPERTIES_NOT_BIND = new IoTxCode(105207, "Set Device Properties error: Device is not bound to this user", "设置设备快照失败：设备未绑定该用户");

    /* queryDeviceStatus (1054) */
    public static IoTxCode ERROR_QUERY_DEVICE_STATUS_NOT_BIND = new IoTxCode(105407, "Query Device Status error: Device is not bound to this user", "查询设备状态失败：设备未绑定该用户");

    /* setDeviceStatus (1055) */
    public static IoTxCode ERROR_SET_DEVICE_STATUS_NOT_BIND = new IoTxCode(105507, "Set Device Status error: Device is not bound to this user", "设置设备状态失败：设备未绑定该用户");

    /* queryDeviceGps (1056) */
    public static IoTxCode ERROR_SET_DEVICE_GPS_NOT_BIND = new IoTxCode(105607, "Query Device Gps error: Device is not bound to this user", "查询设备GPS失败：设备未绑定该用户");

    /* setDefaultDevice (10578) */
    public static IoTxCode ERROR_SET_DEFALUT_DEVICE_NOT_BIND = new IoTxCode(105707, "Set Default Device error: Device is not bound to this user", "设置默认设备失败：设备未绑定该用户");

    /* queryDeviceProperties (1054) */

    /* queryDeviceProperties (1055) */

    /* queryDeviceProperties (1056) */

    /* DeviceBluetoothInfoService 错误码 (107) */
    /* getBluetoothInfo (1071) */
    public static IoTxCode ERROR_GET_DEVICE_BLUETOOTH_HAAS_USER_ID_EMPTY = new IoTxCode(107101, "Get Device Bluetooth Info error: Parameter 'haasUserId' is required", "设备蓝牙信息获取失败：参数'haasUserId'不可为空");
    public static IoTxCode ERROR_GET_DEVICE_BLUETOOTH_INVALID_USER = new IoTxCode(107102, "Get Device Bluetooth Info error: HaaS user not found", "设备蓝牙信息获取失败：HaaS用户不存在");
    public static IoTxCode ERROR_GET_DEVICE_BLUETOOTH_PK_EMPTY = new IoTxCode(107103, "Get Device Bluetooth Info error: Param 'productKey' is required", "设备蓝牙信息获取失败：参数'productKey'不可为空");
    public static IoTxCode ERROR_GET_DEVICE_BLUETOOTH_DN_EMPTY = new IoTxCode(107104, "Get Device Bluetooth Info error: Param 'deviceName' is required", "设备蓝牙信息获取失败：参数'deviceName'不可为空");
    public static IoTxCode ERROR_GET_DEVICE_BLUETOOTH_UNAUTHORIZED = new IoTxCode(107105, "Get Device Bluetooth Info error: This device is not bound to this user", "设备蓝牙信息获取失败：此设备未与该用户绑定或分享");

    /* setBluetoothInfo (1072) */
    public static IoTxCode ERROR_SET_DEVICE_BLUETOOTH_HAAS_USER_ID_EMPTY = new IoTxCode(107201, "Set Device Bluetooth Info error: Parameter 'haasUserId' is required", "设备蓝牙信息设置失败：参数'haasUserId'不可为空");
    public static IoTxCode ERROR_SET_DEVICE_BLUETOOTH_INVALID_USER = new IoTxCode(107202, "Set Device Bluetooth Info error: HaaS user not found", "设备蓝牙信息设置失败：HaaS用户不存在");
    public static IoTxCode ERROR_SET_DEVICE_BLUETOOTH_PK_EMPTY = new IoTxCode(107203, "Set Device Bluetooth Info error: Param 'productKey' is required", "设备蓝牙信息设置失败：参数'productKey'不可为空");
    public static IoTxCode ERROR_SET_DEVICE_BLUETOOTH_DN_EMPTY = new IoTxCode(107204, "Set Device Bluetooth Info error: Param 'deviceName' is required", "设备蓝牙信息设置失败：参数'deviceName'不可为空");
    public static IoTxCode ERROR_SET_DEVICE_BLUETOOTH_UNAUTHORIZED = new IoTxCode(107205, "Set Device Bluetooth Info error: This device is not bound to this user", "设备蓝牙信息设置失败：此设备未与该用户绑定或分享");

    /* TrackGpsService 错误码 (106) */
    /* DeviceTrackList (1061) */
    public static IoTxCode ERROR_DEVICE_TRACK_HAAS_USER_ID_EMPTY = new IoTxCode(106101, "Device Track error: Parameter 'haasUserId' is required", "设备轨迹列表获取失败：参数'haasUserId'不可为空");
    public static IoTxCode ERROR_DEVICE_TRACK_INVALID_USER = new IoTxCode(106102, "Device Track error: HaaS user not found", "设备轨迹列表获取失败：HaaS用户不存在");
    public static IoTxCode ERROR_DEVICE_TRACK_PK_EMPTY = new IoTxCode(106103, "Device Track error: Param 'productKey' is required", "设备轨迹列表获取失败：参数'productKey'不可为空");
    public static IoTxCode ERROR_DEVICE_TRACK_DN_EMPTY = new IoTxCode(106104, "Device Track error: Param 'deviceName' is required", "设备轨迹列表获取失败：参数'deviceName'不可为空");
    public static IoTxCode ERROR_DEVICE_TRACK_UNAUTHORIZED = new IoTxCode(106105, "Device Track error: This device is not bound to this user", "设备轨迹列表获取失败：此设备未与该用户绑定");
    /* queryDeviceTrackGpsDetail (1062) */
    public static IoTxCode ERROR_DEVICE_TRACK_GPS_HAAS_USER_ID_EMPTY = new IoTxCode(106201, "Device Track GPS error: Parameter 'haasUserId' is required", "设备轨迹GPS数据获取失败：参数'haasUserId'不可为空");
    public static IoTxCode ERROR_DEVICE_TRACK_GPS_INVALID_USER = new IoTxCode(106202, "Device Track GPS error: HaaS user not found", "设备轨迹GPS数据获取失败：HaaS用户不存在");
    public static IoTxCode ERROR_DEVICE_TRACK_GPS_PK_EMPTY = new IoTxCode(106203, "Device Track GPS error: Param 'productKey' is required", "设备轨迹GPS数据获取失败：参数'productKey'不可为空");
    public static IoTxCode ERROR_DEVICE_TRACK_GPS_DN_EMPTY = new IoTxCode(106204, "Device Track GPS error: Param 'deviceName' is required", "设备轨迹GPS数据获取失败：参数'deviceName'不可为空");
    public static IoTxCode ERROR_DEVICE_TRACK_GPS_UNAUTHORIZED = new IoTxCode(106205, "Device Track GPS error: This device is not bound to this user", "设备轨迹GPS数据获取失败：此设备未与该用户绑定");


    /* User Default Device (1035) */
    public static IoTxCode ERROR_USER_NOT_SET_DEFAULT_DEVICE = new IoTxCode(103501, "There is no default device for user", "用户未设置默认车辆");
    public static IoTxCode ERROR_USER_SET_MULTI_DEFAULT_DEVICE = new IoTxCode(103502, "There are multi default device for user", "用户设置多个默认车辆异常");

    /* 设备状态异常 (6001) */
    public static IoTxCode ERROR_DEVICE_NOT_ONLINE = new IoTxCode(600101, "Device not online", "设备状态异常：设备不在线");

    /* 车辆控制设备端异常 (701) */
    /* 通用指令 (7010) */
    public static IoTxCode ERROR_VEHICLE_CONTROL_DEVICE_GENERAL = new IoTxCode(701001, "Device general error", "设备控制：通用失败");
    public static IoTxCode ERROR_VEHICLE_CONTROL_DEVICE_GENERAL_0X02 = new IoTxCode(701002, "Device general error", "设备控制：通用失败,错误代码0X02");
    public static IoTxCode ERROR_VEHICLE_CONTROL_DEVICE_GENERAL_0X03 = new IoTxCode(701003, "Device general error", "设备控制：通用失败,错误代码0X03");
    /* 锁车上锁 cmd_opcode = 0x01 (7011) */
    public static IoTxCode ERROR_VEHICLE_CONTROL_DEVICE_RIDING = new IoTxCode(701101, "Device is riding", "设备状态异常：车辆正在骑行中");
    public static IoTxCode ERROR_VEHICLE_CONTROL_DEVICE_UNLOCK = new IoTxCode(701102, "Device is unlock", "设备状态异常：车辆物理钥匙开启");

    /* ADMIN 设备绑定服务 (801) */
    /* 设备解绑 (8012) */
    public static IoTxCode ERROR_UNBIND_ADMIN_PK_EMPTY = new IoTxCode(801201, "ADMIN-Unbind error: Parameter 'productKey' is required", "ADMIN-设备解绑失败：参数'productKey'不可为空");
    public static IoTxCode ERROR_UNBIND_ADMIN_DN_EMPTY = new IoTxCode(801202, "ADMIN-Unbind error: Parameter 'deviceName' is required", "ADMIN-设备解绑失败：参数'deviceName'不可为空");
    public static IoTxCode ERROR_UNBIND_ADMIN_USER_EMPTY = new IoTxCode(801203, "ADMIN-Unbind error: Parameter 'haasUserId' is required", "ADMIN-设备解绑失败：参数'haasUserId'不可为空");
    public static IoTxCode ERROR_UNBIND_ADMIN_NOT_BIND = new IoTxCode(801204, "ADMIN-Unbind error: This device is not bound to this user", "ADMIN-设备解绑失败：此设备未与该用户绑定");
    public static IoTxCode ERROR_UNBIND_ADMIN_INVALID_DEVICE = new IoTxCode(102305, "ADMIN-Unbind error: Device not exists", "ADMIN-设备解绑失败：指定的设备不存在");
    public static IoTxCode ERROR_UNBIND_ADMIN_INVALID_USER = new IoTxCode(102306, "ADMIN-Unbind error: HaaS user not exists", "ADMIN-设备解绑失败：指定的用户不存在");
    public static IoTxCode ERROR_UNBIND_ADMIN_CLEAN_USER = new IoTxCode(801207, "ADMIN-Unbind error: This user is not bound to any devices", "ADMIN-设备解绑失败：此用户未与任何设备绑定");
    public static IoTxCode ERROR_UNBIND_ADMIN_CLEAN_DEVICE = new IoTxCode(801208, "ADMIN-Unbind error: This device is not bound to any user", "ADMIN-设备解绑失败：未检查到此设备与任何用户绑定");

    /* B端服务 (067) */
    /* odm user (0675) */
    public static IoTxCode ERROR_ODM_USER_NOT_SIGN = new IoTxCode(67500, "odm user not sign", "odm用户未签约");
    public static IoTxCode ERROR_ODM_USER_ALREADY_EXIST = new IoTxCode(67501, "odm user already exist", "odm用户已存在");
    public static IoTxCode ERROR_ODM_USER_NOT_EXIST = new IoTxCode(67502, "odm user not exist", "odm用户不存在");
    public static IoTxCode ERROR_ODM_USER_SIGN_FAILED = new IoTxCode(67503, "odm user sign failed", "odm用户签约失败");
    public static IoTxCode ERROR_ODM_PK_NOT_EXIST = new IoTxCode(67504, "pk not exist", "PK不存在");
    public static IoTxCode ERROR_ODM_INVALID_FILE_UPLOAD_PARAM = new IoTxCode(67506, "invalid file upload param", "上传文件参数无效");
    public static IoTxCode ERROR_ODM_UNSUPPORTED_FILE_TYPE = new IoTxCode(67507, "unsupported file type", "不支持的文件类型");
    public static IoTxCode ERROR_ODM_OSS_SIGNATURE_GENERATE_FILED = new IoTxCode(67508, "generate oss signature failed", "OSS签名生成失败");
    public static IoTxCode ERROR_ODM_OSS_FILE_NOT_EXIST = new IoTxCode(67509, "file id is not exist", "文件不存在");
    public static IoTxCode ERROR_ODM_OSS_FILE_UPLOAD_FAIL = new IoTxCode(67510, "upload file to oss failed", "文件上传失败");
    public static IoTxCode ERROR_ODM_OSS_MIGRATEFILE_NOT_EXIST = new IoTxCode(67511, "file is not exist", "文件不存在");
    public static IoTxCode ERROR_ODM_OSS_MIGRATEFILE_CLOSE_FAILED = new IoTxCode(67512, "close file failed", "文件关闭失败");
    public static IoTxCode ERROR_ODM_OSS_MIGRATEFILE_IO_FAILED = new IoTxCode(67513, "file is not exist", "文件访问失败");
    public static IoTxCode ERROR_ODM_INVALID_FILE_DOWNLOAD_PARAM = new IoTxCode(67514, "invalid file download param", "下载文件参数无效");
    public static IoTxCode ERROR_ODM_INVALID_URL_PARAM = new IoTxCode(67515, "adversisement url param", "广告链接错误或图片地址参数错误");
    public static IoTxCode ERROR_ODM_OSS_MIGRATE_FAILED = new IoTxCode(67516, "Migrate File failed", "设备迁移存在错误");


    /* 小二后台(068) */
    public static IoTxCode SYSTEM_ERROR = new IoTxCode(68000, "system error", "系统错误");
    public static IoTxCode ERROR_NOT_FOUND_METHOD = new IoTxCode(68001, "not found method", "请求方法不存在");
    public static IoTxCode ERROR_NOT_FOUND_CLASS = new IoTxCode(68002, "not found class", "未找到请求类");
}

