package com.wenming.library.upload

/**
 * 作者：任宇
 * 日期：2020/4/3 11:21
 * 注释：
 */
enum class UploadStatue(val type: String) {
    SUCCESS("success"),
    NOFILE("nofile"),
    ERROR("error"),
    LOADING("loading"),
    CANCLE("cancle")
}