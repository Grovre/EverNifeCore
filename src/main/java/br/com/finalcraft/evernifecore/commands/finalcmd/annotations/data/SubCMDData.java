package br.com.finalcraft.evernifecore.commands.finalcmd.annotations.data;

import br.com.finalcraft.evernifecore.commands.finalcmd.annotations.FinalCMD;
import br.com.finalcraft.evernifecore.locale.data.FCLocaleData;
import br.com.finalcraft.evernifecore.util.FCReflectionUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.stream.Collectors;


@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class SubCMDData extends CMDData<SubCMDData> {

    public SubCMDData(FinalCMD.SubCMD subCMD) {
        super(subCMD.subcmd(),
                subCMD.usage(),
                subCMD.desc(),
                subCMD.permission(),
                subCMD.context(),
                FCReflectionUtil.getConstructor(subCMD.validation()).invoke(),
                Arrays.stream(subCMD.locales())
                        .map(FCLocaleData::new)
                        .collect(Collectors.toList())
                        .toArray(new FCLocaleData[0])
        );
    }

    public SubCMDData() {
        super();
    }
}
