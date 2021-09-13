package GameEngine;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Game {
    // The window handle
    private long window;
    private  String title = "";
    private int width, height;

    public Game(){
        this.init();
    }

    public Game(String title){
        this.title = title;
        this.init();
    }

    public Game(String title, int width, int height){
        this.title = title;
        this.width = width;
        this.height = height;
        this.init();
    }

    public void run() {

        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(this.width != 0 ? this.width : 300, this.height != 0 ? this.height : 300, this.title.length() > 0 ? this.title : "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void loop() {

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        //Creates VAO and set it to the current one
        int vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        //Give triangle to OGL
        float[] vertices = {
                -1.0f, -1.0f, 0.0f,
                1.0f, -1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
        };
        int vertexBufferId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        //Shaders!
        int vertexShaderId = glCreateShader(GL_VERTEX_SHADER);

        String vertexShaderData = """
                #version 330 core
                layout(location = 0) in vec3 vertexPosition_modelspace;
                void main(){
                  gl_Position.xyz = vertexPosition_modelspace;
                  gl_Position.w = 1.0;
                }
                """;

        glShaderSource(vertexShaderId, vertexShaderData);
        glCompileShader(vertexShaderId);

        IntBuffer vertexShaderStatusBuffer = BufferUtils.createIntBuffer(1);
        glGetShaderiv(vertexShaderId, GL_COMPILE_STATUS, vertexShaderStatusBuffer);
        glGetShaderiv(vertexShaderId, GL_INFO_LOG_LENGTH, vertexShaderStatusBuffer);

        int status = vertexShaderStatusBuffer.get();

        if (status > 0) {
            String err = glGetShaderInfoLog(vertexShaderId, status);
            System.out.println(err);
        }


        int fragmentShaderId = glCreateShader(GL_FRAGMENT_SHADER);

        String fragmentShaderData = """
                #version 330 core
                out vec3 color;
                void main(){
                  color = vec3(0.238,0.238,1);
                }
                """;

        glShaderSource(fragmentShaderId, fragmentShaderData);
        glCompileShader(fragmentShaderId);

        IntBuffer fragmentShaderStatusBuffer = BufferUtils.createIntBuffer(1);
        glGetShaderiv(fragmentShaderId, GL_COMPILE_STATUS, fragmentShaderStatusBuffer);
        glGetShaderiv(fragmentShaderId, GL_INFO_LOG_LENGTH, fragmentShaderStatusBuffer);

        int fragmentstatus = fragmentShaderStatusBuffer.get();

        if (fragmentstatus > 0) {
            String err = glGetShaderInfoLog(fragmentShaderId, fragmentstatus);
            System.out.println(err);
        }

        int programID = glCreateProgram();
        glAttachShader(programID, vertexShaderId);
        glAttachShader(programID, fragmentShaderId);
        glLinkProgram(programID);

        IntBuffer programResultBuffer = BufferUtils.createIntBuffer(1);
        glGetProgramiv(programID, GL_LINK_STATUS, programResultBuffer);
        glGetProgramiv(programID, GL_INFO_LOG_LENGTH, programResultBuffer);
        int programStatus = programResultBuffer.get();

        if (programStatus > 0) {
            System.out.println(glGetProgramInfoLog(programID, programStatus));
        }

        glDetachShader(programID, vertexShaderId);
        glDetachShader(programID, fragmentShaderId);
        glDeleteShader(vertexShaderId);
        glDeleteShader(fragmentShaderId);


        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            //Use our shaders
            glUseProgram(programID);
            //Draw our triangle from VAO
            glEnableVertexAttribArray(0);
            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glDrawArrays(GL_TRIANGLES, 0, 3);
            glDisableVertexAttribArray(0);
            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }
}