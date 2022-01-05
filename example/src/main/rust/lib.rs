use jni::JNIEnv;
use jni::objects::{JClass};
use jni::sys::jstring;

#[no_mangle]
pub extern "system" fn Java_com_example_test_NativeLibrary_hello<'a>(jni: JNIEnv, _: JClass) -> jstring {
    jni.new_string("Hello, World!").unwrap().into_inner()
}
