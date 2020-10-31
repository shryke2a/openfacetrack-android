package com.example.androidopenfacetracking

import android.graphics.Rect
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

class netWork(val sock: DatagramSocket, private val addr: InetSocketAddress) {

    public fun calcAndSend(yaw: Double, pitch: Double, roll: Double, boundingBox: Rect)
    {
        var buf :ByteBuffer = ByteBuffer.allocate(Double.SIZE_BYTES * 6)

        buf.order(ByteOrder.LITTLE_ENDIAN)

        buf.putDouble(0.0) //X
        buf.putDouble(0.0) //Y
        buf.putDouble(0.0) //Z
        //TODO fill with position
        buf.putDouble(yaw)
        buf.putDouble(pitch)
        buf.putDouble(roll)

        sendData(buf.array())
    }

    private fun sendData(array: ByteArray)
    {
        val d: DatagramPacket = DatagramPacket(array, array.size, addr)
        sock.send(d)
    }
}