package br.com.finalcraft.evernifecore.compat.v1_16_R3.worldedit.wrappers.clipboard.format;

import br.com.finalcraft.evernifecore.compat.v1_16_R3.worldedit.wrappers.clipboard.ImpFCBlockArrayClipboard;
import br.com.finalcraft.evernifecore.worldedit.clipboard.FCBlockArrayClipboard;
import br.com.finalcraft.evernifecore.worldedit.clipboard.format.IFCClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class IFCClipboardFormat_Schem extends IFCClipboardFormat {

    @Override
    public Set<String> getAliases() {
        return BuiltInClipboardFormat.SPONGE_SCHEMATIC.getAliases();
    }

    @Override
    public FCBlockArrayClipboard getReaderAndRead(InputStream inputStream) throws IOException {
        return new ImpFCBlockArrayClipboard((BlockArrayClipboard) BuiltInClipboardFormat.SPONGE_SCHEMATIC.getReader(inputStream).read());
    }

    @Override
    public void getWriterAndWrite(OutputStream outputStream, Clipboard clipboard) throws IOException {
        BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(outputStream).write(clipboard);
    }

    @Override
    public boolean isFormat(File file) {
        return BuiltInClipboardFormat.SPONGE_SCHEMATIC.isFormat(file);
    }

}
