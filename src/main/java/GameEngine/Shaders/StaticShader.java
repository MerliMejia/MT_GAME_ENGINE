package GameEngine.Shaders;

public class StaticShader extends ShaderProgram{

    private static final String VERTEX_FILE = "src/main/java/GameEngine/Shaders/vertexShader.glsl";
    private static final String FRAGMENT_FILE = "src/main/java/GameEngine/Shaders/fragmentShader.glsl";

    public StaticShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }
}
