package icu.samnyan.aqua.sega.aimedb

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A new decoder object will be created each time a new request comes in
 */
class AimeDbDecoder : ByteToMessageDecoder() {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(AimeDbDecoder::class.java)
    }

    var length = 0

    /**
     * Decrypt the incoming request including frame management
     * @param ctx ChannelHandlerContext
     * @param input ByteBuf in
     * @param out List<Object>
     */
    override fun decode(ctx: ChannelHandlerContext, input: ByteBuf, out: MutableList<Any>) {
        if (input.readableBytes() < 16) return
        if (length == 0) length = getLength(input)

        if (input.readableBytes() < length) return

        // Create a byte array to store the encrypted data
        val result = AimeDbEncryption.decrypt(input.readBytes(length))

        val resultMap: MutableMap<String, Any> = HashMap()
        resultMap["type"] = result.getShortLE(0x04).toInt()
        resultMap["data"] = result

        logger.debug("AimeDB Request Type: " + resultMap["type"])

        out.add(resultMap)
    }

    /**
     * Get the length from request
     *
     * @param input the request
     * @return int the length of this request
     */
    private fun getLength(input: ByteBuf): Int {
        val currentPos = input.readerIndex()
        val result = AimeDbEncryption.decrypt(input)

        // Check the header
        val header = result.getByte(0).toInt()
        assert(header == 0x3e) { "AimeDB: Invalid header $header" }

        // Read the length from offset 6
        return result.getShortLE(currentPos + 6).toInt()
    }
}
