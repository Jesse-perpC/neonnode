# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides the relative path to your source file(s).
             src/main/cpp/native-lib.cpp )

add_library( # Uses the same # of configurations as the project target.
             libnode
             SHARED
             IMPORTED )

set_target_properties( # Specifies the target library.
                       libnode
                       PROPERTIES IMPORTED_LOCATION
                       # Provides the path to the library you want to import.
                       ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libnode.so )

find_library( # Sets the name of the path variable. : log-lib
              log-lib

              # Specifies the name of the NDK library that
              # CMake should locate.
              log )

target_link_libraries( # Specifies libraries CMake should link to your target library.
                       native-lib
                       libnode
                       ${log-lib} )

target_include_directories( # Specifies the include directories of the native-lib.
                           native-lib
                           PRIVATE
                           ${CMAKE_SOURCE_DIR}/src/main/cpp/include )
