package me.t3sl4.hydraulic.launcher.utils.Version;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class PersistentService extends Service<Void> {

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                while (true) {
                    System.out.println("Servis çalışıyor...");

                    // Örneğin, her 5 saniyede bir çalıştır
                    Thread.sleep(5000);
                }
            }
        };
    }
}
