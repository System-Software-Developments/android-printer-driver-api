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

import com.aevi.print.model.PrinterStatus;
import com.aevi.print.model.PrintingContext;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class PrinterStatusStream {

    private static final Map<String, PublishSubject<PrinterStatus>> PRINTER_STATUS_STREAM_MAP = new HashMap<>();

    public static void emitStatus(String printerId, String printerStatus) {
        if (PRINTER_STATUS_STREAM_MAP.containsKey(printerId)) {
            PRINTER_STATUS_STREAM_MAP.get(printerId).onNext(new PrinterStatus(printerStatus));
        }
    }

    public static void finishPrinter(String printerId) {
        if (PRINTER_STATUS_STREAM_MAP.containsKey(printerId)) {
            PublishSubject<PrinterStatus> printerStatusStream = PRINTER_STATUS_STREAM_MAP.get(printerId);
            printerStatusStream.onComplete();
            PRINTER_STATUS_STREAM_MAP.remove(printerId);
        }
    }

    protected void subscribeToStatus(final PrintingContext printingContext, final String printerId) {
        synchronized (PRINTER_STATUS_STREAM_MAP) {
            PublishSubject<PrinterStatus> printerStatusStream;
            if (PRINTER_STATUS_STREAM_MAP.containsKey(printerId)) {
                printerStatusStream = PRINTER_STATUS_STREAM_MAP.get(printerId);
            } else {
                printerStatusStream = PublishSubject.create();
                PRINTER_STATUS_STREAM_MAP.put(printerId, printerStatusStream);
            }

            printerStatusStream.subscribe(new Observer<PrinterStatus>() {
                Disposable disposable;

                @Override
                public void onSubscribe(@NonNull Disposable d) {
                    this.disposable = d;
                }

                @Override
                public void onNext(@NonNull PrinterStatus printerStatus) {
                    if (!printingContext.send(printerStatus.toJson())) {
                        disposable.dispose();
                    }
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    finish();
                }

                @Override
                public void onComplete() {
                    finish();
                }

                private void finish() {
                    printingContext.sendEndStream();
                    disposable.dispose();
                }
            });
        }
    }
}
