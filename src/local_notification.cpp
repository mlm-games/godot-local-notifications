#include "local_notification.hpp"
#include "android_utils.hpp"

#include <godot_cpp/core/class_db.hpp>
#include <godot_cpp/variant/utility_functions.hpp>

using namespace godot;

LocalNotification* LocalNotification::singleton = nullptr;

LocalNotification* LocalNotification::get_singleton() {
    return singleton;
}

LocalNotification::LocalNotification() {
    singleton = this;
}

LocalNotification::~LocalNotification() {
    singleton = nullptr;
}

void LocalNotification::_bind_methods() {
    ClassDB::bind_method(D_METHOD("is_permission_granted"), &LocalNotification::is_permission_granted);
    ClassDB::bind_method(D_METHOD("request_permission"), &LocalNotification::request_permission);
    ClassDB::bind_method(D_METHOD("open_app_setting"), &LocalNotification::open_app_setting);
    ClassDB::bind_method(D_METHOD("show", "title", "message", "interval", "tag"), &LocalNotification::show);
    ClassDB::bind_method(D_METHOD("show_repeating", "title", "message", "interval", "repeat_interval", "tag"), &LocalNotification::show_repeating);
    ClassDB::bind_method(D_METHOD("show_daily", "title", "message", "at_hour", "at_minute", "tag"), &LocalNotification::show_daily);
    ClassDB::bind_method(D_METHOD("cancel", "tag"), &LocalNotification::cancel);

    ADD_SIGNAL(MethodInfo("on_permission_request_completed"));
}

bool LocalNotification::is_permission_granted() {
    return AndroidUtils::call_static_method("isPermissionGranted");
}

void LocalNotification::request_permission() {
    AndroidUtils::call_static_method("requestPermission");
}

void LocalNotification::open_app_setting() {
    AndroidUtils::call_static_method("openAppSetting");
}

void LocalNotification::show(const String &title, const String &message, int interval, int tag) {
    AndroidUtils::call_static_method("show", title, message, interval, tag);
}

void LocalNotification::show_repeating(const String &title, const String &message, int interval, int repeat_interval, int tag) {
    AndroidUtils::call_static_method("showRepeating", title, message, interval, repeat_interval, tag);
}

void LocalNotification::show_daily(const String &title, const String &message, int at_hour, int at_minute, int tag) {
    AndroidUtils::call_static_method("showDaily", title, message, at_hour, at_minute, tag);
}

void LocalNotification::cancel(int tag) {
    AndroidUtils::call_static_method("cancel", tag);
}