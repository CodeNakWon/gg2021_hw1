package com.example.gg2021_hw1

import android.content.Context
import android.icu.number.Scale
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.nio.charset.Charset
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MainActivity : AppCompatActivity() {
    private lateinit var glView: GLSurfaceView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glView = MyGLSurfaceView(this)

        setContentView(glView)
    }
}

class MyGLSurfaceView(context: Context): GLSurfaceView(context){
    private val renderer: MyGLRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = MyGLRenderer(context)
        setRenderer(renderer)
    }
}

class MyGLRenderer(context: Context): GLSurfaceView.Renderer{

    private val mContext: Context = context
    private var vPMatrix = FloatArray(16)
    private var projectionMatrix = FloatArray(16)
    private var viewMatrix = FloatArray(16)

    //P. model matrix & 매 프레임 변화 matrix 선언
    private val cubeMatrix: FloatArray = FloatArray(16)
    private val personMatrix: FloatArray = FloatArray(16)
    private val teapotMatrix: FloatArray = FloatArray(16)

    private val rotMatrix: FloatArray = FloatArray(16)
    private val growMatrix: FloatArray = FloatArray(16)

    //P. object 선언
    private lateinit var cube: Obj
    private lateinit var person: Obj
    private lateinit var teapot: Obj

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        //P. object 초기화

        cube = Obj(mContext,"cube.obj")
        person = Obj(mContext,"person.obj")
        teapot = Obj(mContext,"teapot.obj")
        //P. model matrix & 매 프레임 변화 matrix 초기화
        srtMatrix(personMatrix, transX = 2f)
        srtMatrix(cubeMatrix, scaleX = 0.5f, scaleZ = 0.5f)
        srtMatrix(teapotMatrix, scaleX = 0.2f, scaleY = 0.2f, scaleZ = 0.2f, transX = 1.24f, transY = 0.4f, rotY = 180f)
        srtMatrix(rotMatrix, rotY = 0.8f )
        srtMatrix(growMatrix, scaleX = 1.001f, scaleY = 1.002f, scaleZ = 1.001f )
    }
    

    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        //P. 아래 구현한 mySetLookAtM function 으로 수정
        mySetLookAtM(viewMatrix, 1.5f, 1.5f, -9f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //Matrix.setLookAtM(viewMatrix, 0, 1.0f, 1.0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //P. 각 object 별 매 프레임 변화 matrix 와 model matrix 를 multiply
        if(cubeMatrix[5] <= 3.0f) {
            Matrix.multiplyMM(cubeMatrix, 0, growMatrix, 0, cubeMatrix, 0)
        }
        Matrix.multiplyMM(personMatrix, 0, rotMatrix, 0, personMatrix, 0)
        Matrix.multiplyMM(teapotMatrix, 0, rotMatrix, 0, teapotMatrix, 0)
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        //P. object draw
        cube.draw(vPMatrix,cubeMatrix)
        person.draw(vPMatrix,personMatrix)
        teapot.draw(vPMatrix,teapotMatrix)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0,0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

        //P.  아래 구현한 myFrustumM function 으로 수정
        myFrustumM(projectionMatrix, ratio, 60f, 2f, 12f)
        //myFrustumM2(projectionMatrix, -ratio, ratio, -1f, 1f, 2f, 10f)
        //Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 2f, 10f)
    }
}
//P. vecNormalize function 구현: 벡터 정규화 함수 (mySetLookAtM function 구현 시 사용)
fun vecNormalize(vec : FloatArray ){
    val n : Float = kotlin.math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2])
    vec[0] = vec[0]/n
    vec[1] = vec[1]/n
    vec[2] = vec[2]/n
}
//P. mySetLookAtM function 구현: viewMatrix 구하는 함수 (Matrix library function 중 multiplyMM 만 사용 가능)
fun mySetLookAtM(result: FloatArray, eyeX: Float, eyeY: Float, eyeZ: Float, centerX: Float, centerY: Float, centerZ: Float, upX: Float, upY: Float, upZ: Float){
    val nVec : FloatArray = floatArrayOf(eyeX-centerX, eyeY-centerY, eyeZ-centerZ)
    vecNormalize(nVec)
    val uVec : FloatArray = floatArrayOf(upY * nVec[2] - upZ * nVec[1],
                                         upZ * nVec[0] - upX * nVec[2],
                                         upX * nVec[1] - upY * nVec[0])
    vecNormalize(uVec)
    val vVec : FloatArray = floatArrayOf(nVec[1] * uVec[2] - nVec[2] * uVec[1],
                                         nVec[2] * uVec[0] - nVec[0] * uVec[2],
                                         nVec[0] * uVec[1] - nVec[1] * uVec[0])
    vecNormalize(vVec)

    result[0] = uVec[0]; result[4] = uVec[1]; result[8] = uVec[2];  result[12] = -eyeX * uVec[0] - eyeY * uVec[1] - eyeZ * uVec[2]
    result[1] = vVec[0]; result[5] = vVec[1]; result[9] = vVec[2];  result[13] = -eyeX * vVec[0] - eyeY * vVec[1] - eyeZ * vVec[2]
    result[2] = nVec[0]; result[6] = nVec[1]; result[10] = nVec[2]; result[14] = -eyeX * nVec[0] - eyeY * nVec[1] - eyeZ * nVec[2]
    result[3] = 0f;      result[7] = 0f;      result[11] = 0f;      result[15] = 1f
}
//P. myFrustumM function 구현: projectionMatrix 구하는 함수 (Matrix library function 중 multiplyMM 만 사용 가능)
fun myFrustumM(result: FloatArray, aspect: Float, fov: Float,  near: Float, far: Float){
    result[5] = kotlin.math.tan(fov * kotlin.math.PI / 180).toFloat()
    result[0] = result[5]/aspect
    result[10] = (far+near)/(near-far)
    result[11] = -1f
    result[14] = 2*near*far/(near-far)
}
fun myFrustumM2(result: FloatArray, left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float){
    result[0] = 2*near/(right-left)
    result[5] = 2*near/(top-bottom)
    result[10] = (far+near)/(near-far)
    result[11] = -1f
    result[14] = 2*near*far/(near-far)
}

fun srtMatrix(result: FloatArray, scaleX: Float = 1f, scaleY: Float = 1f, scaleZ: Float = 1f, rotX : Float = 0f, rotY : Float = 0f, rotZ : Float = 0f, transX : Float = 0f, transY : Float = 0f, transZ : Float = 0f ){
    println("작동")
    val radianX : Float = (rotX * kotlin.math.PI/180).toFloat()
    val radianY : Float = (rotY * kotlin.math.PI/180).toFloat()
    val radianZ : Float = (rotZ * kotlin.math.PI/180).toFloat()
    val sMatrix = floatArrayOf(scaleX, 0f, 0f, 0f, 0f, scaleY, 0f, 0f, 0f, 0f, scaleZ, 0f, 0f, 0f, 0f, 1f)
    val xRMatrix = floatArrayOf(1f, 0f, 0f, 0f, 0f, kotlin.math.cos(radianX), kotlin.math.sin(radianX), 0f, 0f,-kotlin.math.sin(radianX), kotlin.math.cos(radianX), 0f, 0f, 0f, 0f, 1f)
    val yRMatrix = floatArrayOf(kotlin.math.cos(radianY), 0f, kotlin.math.sin(radianY), 0f, 0f, 1f, 0f, 0f, -kotlin.math.sin(radianY), 0f, kotlin.math.cos(radianY), 0f, 0f, 0f, 0f, 1f)
    val zRMatrix = floatArrayOf(kotlin.math.cos(radianZ), -kotlin.math.sin(radianZ), 0f, 0f, kotlin.math.sin(radianZ), kotlin.math.cos(radianZ), 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
    val tMatrix = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, transX, transY, transZ, 1f)

    Matrix.multiplyMM(result, 0, xRMatrix, 0, sMatrix, 0)
    Matrix.multiplyMM(result, 0, yRMatrix, 0, result, 0)
    Matrix.multiplyMM(result, 0, zRMatrix, 0, result, 0)
    Matrix.multiplyMM(result, 0, tMatrix, 0, result, 0)
}

//PP. cube, person, teapot 모두 포함할 수 있는 Object class 로 수정
class Obj(context: Context, filename: String){

    //P. 아래 shader code string 지우고, res/raw 에 위치한 vertex.glsl , fragment.glsl 로드해서 vertexShaderCode, fragmentShaderCode 에 넣기
    private val vertexShaderStream = context.resources.openRawResource(R.raw.vertex)
    private val vertexShaderCode = vertexShaderStream.readBytes().toString(Charset.defaultCharset())

    private val fragmentShaderStream = context.resources.openRawResource(R.raw.fragment)
    private val fragmentShaderCode = fragmentShaderStream.readBytes().toString(Charset.defaultCharset())

    //P. model matrix handle 변수 추가 선언
    private var vPMatrixHandle: Int = 0

    val color = floatArrayOf(1.0f, 0.980392f, 0.980392f, 0.3f)

    private var mProgram: Int
    private var vertices = mutableListOf<Float>()
    private var faces = mutableListOf<Short>()
    private lateinit var verticesBuffer: FloatBuffer
    private lateinit var facesBuffer: ShortBuffer

    init {
        try {
            val scanner = Scanner(context.assets.open(filename))
            while (scanner.hasNextLine()){
                val line = scanner.nextLine()
                if (line.startsWith("v  ")){
                    val vertex = line.split(" ")
                    val x = vertex[2].toFloat()
                    val y = vertex[3].toFloat()
                    val z = vertex[4].toFloat()
                    vertices.add(x)
                    vertices.add(y)
                    vertices.add(z)
                }
                else if (line.startsWith("f ")) {
                    val face = line.split(" ")
                    val vertex1 = face[1].split("/")[0].toShort()
                    val vertex2 = face[2].split("/")[0].toShort()
                    val vertex3 = face[3].split("/")[0].toShort()
                    faces.add(vertex1)
                    faces.add(vertex2)
                    faces.add(vertex3)
                }
            }

            verticesBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    for (vertex in vertices){
                        put(vertex)
                    }
                    position(0)
                }
            }

            facesBuffer = ByteBuffer.allocateDirect(faces.size * 2).run {
                order(ByteOrder.nativeOrder())
                asShortBuffer().apply {
                    for (face in faces){
                        put((face-1).toShort())
                    }
                    position(0)
                }
            }
        } catch (e: Exception){
            Log.e("file_read", e.message.toString())
        }

        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        mProgram = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    val COORDS_PER_VERTEX = 3
    var wMatrix = FloatArray(16)

    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0
    private var worldHandle: Int = 0
    private val vertexStride: Int = COORDS_PER_VERTEX * 4


    //PP. cube, person, teapot 의 world transform 및 매 프레임 변화를 반영할 수 있는 draw function 으로 수정
    fun draw(mvpMatrix: FloatArray, mdlMatrix : FloatArray){

        GLES20.glUseProgram(mProgram)

        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,false, vertexStride, verticesBuffer)

        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
        GLES20.glUniform4fv(mColorHandle,1,color,0)

        worldHandle = GLES20.glGetUniformLocation(mProgram, "worldMatrix")
        GLES20.glUniformMatrix4fv(worldHandle,1,false, mdlMatrix,0)

        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, faces.size, GLES20.GL_UNSIGNED_SHORT, facesBuffer)

        GLES20.glDisableVertexAttribArray(positionHandle)

    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}