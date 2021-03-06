/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aevi.print.driver;

import com.aevi.android.rxmessenger.ChannelServer;
import com.aevi.android.rxmessenger.service.AbstractChannelService;
import com.aevi.print.model.ChannelPrintingContext;
import com.aevi.print.model.PrintingContext;

import io.reactivex.functions.Consumer;

/**
 * This abstract service should be extended to provide a print driver status implementation
 *
 * @see com.aevi.print.driver.common.service.CommonPrinterStatusService
 */
public abstract class BasePrinterStatusService extends AbstractChannelService {

    private final PrinterStatusStream printerStatusStream;

    protected BasePrinterStatusService() {
        printerStatusStream = new PrinterStatusStream();
    }

    @Override
    protected void onNewClient(ChannelServer channelServer, final String callingPackageName) {
        final PrintingContext printingContext = new ChannelPrintingContext(channelServer);
        channelServer.subscribeToMessages().subscribe(new Consumer<String>() {
            @Override
            public void accept(String statusRequest) {
                handleRequest(printingContext, statusRequest, callingPackageName);
            }
        });
    }

    public void handleRequest(PrintingContext printingContext, String statusRequest, String packageName) {
        printerStatusStream.subscribeToStatus(printingContext, statusRequest);
    }
}
