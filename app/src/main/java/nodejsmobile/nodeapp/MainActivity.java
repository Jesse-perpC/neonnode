package nodejsmobile.nodeapp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {

    private static AssetManager assetManager = null;
    private static String TAG = "NodeApp";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("node");
    }

    private static final String DEFAULT_SCRIPT = "main.js";
    private static final String RUNTIME_DIR = "nodejs";

    private String nodeArgs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the args for node
        Intent intent = getIntent();
        nodeArgs = intent.getStringExtra("nodeargs");

        if (nodeArgs == null) {
            // No input args, copying assets...
            assetManager = this.getAssets();
            try {
                copyAssets();
            } catch (IOException e) {
                e.printStackTrace();
                Log.v(TAG, "COPYASSETS:FAIL");
                return;
            }
            Log.v(TAG, "COPYASSETS:PASS");

            // Run the default node app that was copied into the writable directory.
            nodeArgs = this.getFilesDir().getAbsolutePath() + "/" + RUNTIME_DIR + "/" + DEFAULT_SCRIPT;
        }

        final String args = "node " + nodeArgs;
        runNodeOnBackgroundThread(args);
    }

    public native Integer startNodeWithArguments(String[] arguments, String nodePath, boolean redirectOutputToLogcat);

    private void runNodeOnBackgroundThread(final String args) {
        Log.v(TAG, "Args: " + args);

        final String[] parts = args.split(" ");
        final String nodePath = this.getFilesDir().getAbsolutePath() + "/" + RUNTIME_DIR;

        Thread mainNodeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.startNodeWithArguments(
                        parts,
                        nodePath,
                        true);
            }
        });
        mainNodeThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            //If the node thread throws some exception, let's say the test fails.
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Log.i(TAG, "RESULT:FAIL");
            }
        });
        mainNodeThread.start();
    }

    private void copyAssets() throws IOException {
        String destFolder = this.getFilesDir().getAbsolutePath() + "/" + RUNTIME_DIR;
        File folderObject = new File(destFolder);
        if (folderObject.exists()) {
            deleteFolderRecursively(folderObject);
        }
        enumerateAssetFolder("", destFolder);
    }

    private void enumerateAssetFolder(String srcFolder, String destPath) throws IOException {
        String[] files = assetManager.list(srcFolder);

        if (files.length == 0) {
            copyAssetFile(srcFolder, destPath);
        } else {
            new File(destPath).mkdirs();
            for (String file : files) {
                if (srcFolder.equals("")) {
                    enumerateAssetFolder(file, destPath + "/" + file);
                } else {
                    enumerateAssetFolder(srcFolder + "/" + file, destPath + "/" + file);
                }
            }
        }
    }

    private void copyAssetFile(String srcFolder, String destPath) throws IOException {
        InputStream in = assetManager.open(srcFolder);
        new File(destPath).createNewFile();
        OutputStream out = new FileOutputStream(destPath);
        copyFile(in, out);
        in.close();
        in = null;
        out.flush();
        out.close();
        out = null;
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void deleteFolderRecursively(File file) throws IOException {
        for (File childFile : file.listFiles()) {
            if (childFile.isDirectory()) {
                deleteFolderRecursively(childFile);
            } else {
                childFile.delete();
            }
        }
        file.delete();
    }
}
