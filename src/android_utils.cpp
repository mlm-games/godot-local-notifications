#include "android_utils.hpp"
#include "local_notification.hpp"

#include <godot_cpp/core/class_db.hpp>
#include <godot_cpp/classes/os.hpp>
#include <godot_cpp/classes/engine.hpp>

#ifdef __ANDROID__
#include <jni.h>
#include <android/log.h>
#include <godot_cpp/core/engine.hpp>

// Cache JNI variables to improve performance
static jobject godot_instance;
static jclass local_notification_class;
static jmethodID is_permission_granted_method;
static jmethodID request_permission_method;
static jmethodID open_app_setting_method;
static jmethodID show_method;
static jmethodID show_repeating_method;
static jmethodID show_daily_method;
static jmethodID cancel_method;
#endif

using namespace godot;

bool AndroidUtils::call_static_method(const String& method_name) {
#ifdef __ANDROID__
    if (Engine::get_singleton()->is_editor_hint()) {
        return false;
    }

    JNIEnv *env = static_cast<JNIEnv*>(OS::get_singleton()->get_native_handle("android_env"));
    if (!env) {
        return false;
    }
    
    if (!godot_instance) {
        jobject activity = static_cast<jobject>(OS::get_singleton()->get_native_handle("android_activity"));
        jclass godot_class = env->FindClass("org/godotengine/godot/Godot");
        jmethodID get_instance = env->GetStaticMethodID(godot_class, "getInstance", "()Lorg/godotengine/godot/Godot;");
        godot_instance = env->NewGlobalRef(env->CallStaticObjectMethod(godot_class, get_instance));
        
        // Get the plugin class
        jmethodID get_plugin = env->GetMethodID(godot_class, "getPlugin", "(Ljava/lang/String;)Lorg/godotengine/godot/plugin/GodotPlugin;");
        jobject plugin = env->CallObjectMethod(godot_instance, get_plugin, env->NewStringUTF("LocalNotification"));
        
        if (!plugin) {
            return false;
        }
        
        // Cache the class and method IDs
        jclass cls = env->GetObjectClass(plugin);
        local_notification_class = (jclass)env->NewGlobalRef(cls);
        
        is_permission_granted_method = env->GetMethodID(local_notification_class, "isPermissionGranted", "()Z");
        request_permission_method = env->GetMethodID(local_notification_class, "requestPermission", "()V");
        open_app_setting_method = env->GetMethodID(local_notification_class, "openAppSetting", "()V");
        show_method = env->GetMethodID(local_notification_class, "show", "(Ljava/lang/String;Ljava/lang/String;II)V");
        show_repeating_method = env->GetMethodID(local_notification_class, "showRepeating", "(Ljava/lang/String;Ljava/lang/String;III)V");
        show_daily_method = env->GetMethodID(local_notification_class, "showDaily", "(Ljava/lang/String;Ljava/lang/String;III)V");
        cancel_method = env->GetMethodID(local_notification_class, "cancel", "(I)V");
    }
    
    if (method_name == "isPermissionGranted") {
        return env->CallBooleanMethod(godot_instance, is_permission_granted_method);
    } else if (method_name == "requestPermission") {
        env->CallVoidMethod(godot_instance, request_permission_method);
        return true;
    } else if (method_name == "openAppSetting") {
        env->CallVoidMethod(godot_instance, open_app_setting_method);
        return true;
    }
#endif
    return false;
}

bool AndroidUtils::call_static_method(const String& method_name, const String& arg1, const String& arg2, int arg3, int arg4) {
#ifdef __ANDROID__
    if (Engine::get_singleton()->is_editor_hint()) {
        return false;
    }

    JNIEnv *env = static_cast<JNIEnv*>(OS::get_singleton()->get_native_handle("android_env"));
    if (!env || !godot_instance) {
        return false;
    }
    
    if (method_name == "show") {
        jstring j_title = env->NewStringUTF(arg1.utf8().get_data());
        jstring j_message = env->NewStringUTF(arg2.utf8().get_data());
        env->CallVoidMethod(godot_instance, show_method, j_title, j_message, arg3, arg4);
        env->DeleteLocalRef(j_title);
        env->DeleteLocalRef(j_message);
        return true;
    }
#endif
    return false;
}

bool AndroidUtils::call_static_method(const String& method_name, const String& arg1, const String& arg2, int arg3, int arg4, int arg5) {
#ifdef __ANDROID__
    if (Engine::get_singleton()->is_editor_hint()) {
        return false;
    }

    JNIEnv *env = static_cast<JNIEnv*>(OS::get_singleton()->get_native_handle("android_env"));
    if (!env || !godot_instance) {
        return false;
    }
    
    if (method_name == "showRepeating") {
        jstring j_title = env->NewStringUTF(arg1.utf8().get_data());
        jstring j_message = env->NewStringUTF(arg2.utf8().get_data());
        env->CallVoidMethod(godot_instance, show_repeating_method, j_title, j_message, arg3, arg4, arg5);
        env->DeleteLocalRef(j_title);
        env->DeleteLocalRef(j_message);
        return true;
    } else if (method_name == "showDaily") {
        jstring j_title = env->NewStringUTF(arg1.utf8().get_data());
        jstring j_message = env->NewStringUTF(arg2.utf8().get_data());
        env->CallVoidMethod(godot_instance, show_daily_method, j_title, j_message, arg3, arg4, arg5);
        env->DeleteLocalRef(j_title);
        env->DeleteLocalRef(j_message);
        return true;
    }
#endif
    return false;
}

bool AndroidUtils::call_static_method(const String& method_name, int arg1) {
#ifdef __ANDROID__
    if (Engine::get_singleton()->is_editor_hint()) {
        return false;
    }

    JNIEnv *env = static_cast<JNIEnv*>(OS::get_singleton()->get_native_handle("android_env"));
    if (!env || !godot_instance) {
        return false;
    }
    
    if (method_name == "cancel") {
        env->CallVoidMethod(godot_instance, cancel_method, arg1);
        return true;
    }
#endif
    return false;
}

void AndroidUtils::emit_signal_permission_completed() {
    if (LocalNotification::get_singleton()) {
        LocalNotification::get_singleton()->emit_signal("on_permission_request_completed");
    }
}