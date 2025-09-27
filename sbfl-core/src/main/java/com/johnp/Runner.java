package com.johnp;

import com.johnp.bean.MethodInfo;
import com.johnp.util.Analyzer;
import com.johnp.util.FileExportUtil;
import com.johnp.util.SuspicionProcessor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
public class Runner {

    public void runSbfl(String pathname, int failCount) throws IOException {

        File folder = new File(pathname);

        // ** Read and Collect Data
        Map<String, MethodInfo> calculatedSuspicion = Analyzer.analyzeFolder(folder, failCount);

        // ** Sort Suspicion
        List<Map.Entry<String, MethodInfo>> sortedDataList = SuspicionProcessor.sortSuspicion(calculatedSuspicion);

//        FileExportUtil.xlsExport(pathname + "/Suspicion.xlsx", sortedDataList);

        FileExportUtil.csvExport(pathname + "/Suspicion.csv", sortedDataList);

        System.out.println("Completed Exporting Suspicion Data.");

    }


}