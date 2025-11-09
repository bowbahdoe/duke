import dev.mccue.duke.Duke;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class SpriteSheet {
    @Test
    public void generateSpriteSheet() throws Exception {
        Files.createDirectories(Path.of("demo"));
        var sb = new StringBuilder();
        sb.append("""
                <!doctype html>
                <html lang="en-US">
                  <head>
                    <meta charset="utf-8" />
                    <title>Dukes Demo</title>
                    <meta name="viewport" content="width=device-width" />
                  </head>
                  <body style="background-color: tan">""");


        var r = new Random(0);
        for (int i = 0; i < 10000; i++) {
            var duke = new Duke(r.nextLong());
            var img = duke.toBufferedImage_256x256();

            ImageIO.write(
                    img,
                    "png",
                    new File("demo/out-" + i + ".png")
            );

            sb.append("<img width=\"32px\" height=\"32px\" src=/out-" + i + ".png />");

        }

        sb.append("""
                  </body>
                </html>
                """);
        Files.writeString(
                Path.of("demo/index.html"),
                sb.toString()
        );

    }
}
