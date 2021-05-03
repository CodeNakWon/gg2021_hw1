uniform mat4 uMVPMatrix;
attribute vec4 vPosition;
uniform mat4 worldMatrix;

void main() {
    gl_Position = uMVPMatrix * worldMatrix * vPosition;
}