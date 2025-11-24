#ifndef LOCAL_NOTIFICATION_H
#define LOCAL_NOTIFICATION_H

#include <godot_cpp/classes/object.hpp>

namespace godot {

class LocalNotification : public Object {
    GDCLASS(LocalNotification, Object);

protected:
    static void _bind_methods();

private:
    static LocalNotification *singleton;

public:
    static LocalNotification *get_singleton();
    
    bool is_permission_granted();
    void request_permission();
    void open_app_setting();
    void show(const String &title, const String &message, int interval, int tag);
    void show_repeating(const String &title, const String &message, int interval, int repeat_interval, int tag);
    void show_daily(const String &title, const String &message, int at_hour, int at_minute, int tag);
    void cancel(int tag);

    LocalNotification();
    ~LocalNotification();
};

}
#endif