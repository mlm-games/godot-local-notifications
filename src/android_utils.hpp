#ifndef ANDROID_UTILS_H
#define ANDROID_UTILS_H

#include <godot_cpp/variant/variant.hpp>
#include <godot_cpp/variant/string.hpp>

namespace godot {

class AndroidUtils {
public:
    static bool call_static_method(const String& method_name);
    static bool call_static_method(const String& method_name, const String& arg1, const String& arg2, int arg3, int arg4);
    static bool call_static_method(const String& method_name, const String& arg1, const String& arg2, int arg3, int arg4, int arg5);
    static bool call_static_method(const String& method_name, int arg1);
    
    static void emit_signal_permission_completed();
};

}
#endif