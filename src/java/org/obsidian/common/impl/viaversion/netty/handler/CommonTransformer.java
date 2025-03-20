package org.obsidian.common.impl.viaversion.netty.handler;

import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.lang.reflect.InvocationTargetException;

public class CommonTransformer {

    public static void decompress(ChannelHandlerContext ctx, ByteBuf buf) throws InvocationTargetException {
        ChannelHandler handler = ctx.pipeline().get("decompress");
        ByteBuf decompressed = handler instanceof MessageToMessageDecoder<?> wrapper ? (ByteBuf) PipelineUtil.callDecode(wrapper, ctx, buf).get(0) : (ByteBuf) PipelineUtil.callDecode((ByteToMessageDecoder) handler, ctx, buf).get(0);
        try {
            buf.clear().writeBytes(decompressed);
        } finally {
            decompressed.release();
        }
    }

    public static void compress(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        ChannelHandler handler = ctx.pipeline().get("compress");
        ByteBuf compressed = ctx.alloc().buffer();
        try {
            if (handler instanceof MessageToByteEncoder<?> wrapper) {
                PipelineUtil.callEncode(wrapper, ctx, buf, compressed);
                buf.clear().writeBytes(compressed);
            }
        } finally {
            compressed.release();
        }
    }
}

