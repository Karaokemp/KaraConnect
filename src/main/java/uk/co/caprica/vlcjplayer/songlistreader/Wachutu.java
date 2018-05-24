package uk.co.caprica.vlcjplayer.songlistreader;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.nio.file.SensitivityWatchEventModifier;

import uk.co.caprica.vlcjplayer.songlistreader.KaraokeReader.KaraFilter;

@SuppressWarnings("restriction")
public class Wachutu {
	
	public static final Logger LOGGER = Logger.getLogger(Wachutu.class.getName());

    private static File rootFolder;

    private static WatchService watcher;

    private static ExecutorService executor;
    
    private static final KaraFilter filter = new KaraFilter();
    
    public static void init(String root) throws IOException {
    	try {
			rootFolder = new File(root);
			watcher = FileSystems.getDefault().newWatchService();
			executor = Executors.newSingleThreadExecutor();
			startRecursiveWatcher();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
    }

    public static void cleanup() {
        try {
            watcher.close();
        } catch (IOException e) {
        	LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        try {
			executor.shutdown();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
    }

    @SuppressWarnings("unchecked")
	private static void startRecursiveWatcher() throws IOException {

        final Map<WatchKey, Path> keys = new HashMap<>();

        Consumer<Path> register = p -> {
            if (!p.toFile().exists() || !p.toFile().isDirectory()) {
                throw new RuntimeException("folder " + p + " does not exist or is not a directory");
            }
            try {
                Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        LOGGER.info("registering " + dir + " in watcher service");
                        WatchKey watchKey = dir.register(watcher, new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE}, SensitivityWatchEventModifier.HIGH);
                        keys.put(watchKey, dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException("Error registering path " + p, e);
            }
        };

        register.accept(rootFolder.toPath());

        executor.submit(() -> {
            while (true) {
                final WatchKey key;
                try {
                    key = watcher.take(); // wait for a key to be available
                } catch (InterruptedException ex) {
                    return;
                }

                final Path dir = keys.get(key);
                if (dir == null) {
                    LOGGER.log(Level.SEVERE, "WatchKey " + key + " not recognized!");
                    continue;
                }

                key.pollEvents().stream()
                        .filter(e -> (e.kind() != OVERFLOW))
                        .map(e -> ((WatchEvent<Path>) e).context())
                        .forEach(p -> {
                            final Path absPath = dir.resolve(p);
                            if (absPath.toFile().isDirectory()) {
                                register.accept(absPath);
                            } else {
                                final File f = absPath.toFile();
                                LOGGER.info("Detected new file " + f.getAbsolutePath());
                                if (filter.accept(f)) {
                                	DB.insertFiles(f.getAbsolutePath());
                                	LOGGER.info("Added new file " + f.getAbsolutePath());
                                }
                            }
                        });

                boolean valid = key.reset(); // IMPORTANT: The key must be reset after processed
                if (!valid) {
                    break;
                }
            }
        });
    }
}
