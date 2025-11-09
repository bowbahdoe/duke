package dev.mccue.duke;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import dev.mccue.imgscalr.Scalr;

import dev.mccue.guava.hash.Hashing;

public final class Duke {
    private static final List<BufferedImage> NOSES;
    private static final List<BufferedImage> HEADS;
    private static final List<BufferedImage> BODIES;

    static {
        try {
            BufferedImage spriteSheet;
            try (var is = Duke.class.getResourceAsStream("/dev/mccue/duke/dukes.png")) {
                spriteSheet = ImageIO.read(Objects.requireNonNull(is));
            }
            List<BufferedImage> parts = new ArrayList<>();
            for (int j = 0; j < 18; j++) {
                for (int i = 0; i < 17; i++) {
                    parts.add(spriteSheet.getSubimage(i * 32, j * 32, 32, 32));
                }
            }

            List<BufferedImage> heads = parts.subList(1, 90);
            List<BufferedImage> noses = parts.subList(91, 146);
            List<BufferedImage> bodies = parts.subList(147, 300);


            /*
            for (int i = 0; i < heads.size(); i++) {
                var head = heads.get(i);
                ImageIO.write(head, "png", new File("head-" + i + ".png"));
            }

            for (int i = 0; i < noses.size(); i++) {
                var nose = noses.get(i);
                ImageIO.write(nose, "png", new File("nose-" + i + ".png"));
            }

            for (int i = 0; i < bodies.size(); i++) {
                var body = bodies.get(i);
                ImageIO.write(body, "png", new File("body-" + i + ".png"));
            }
            */

            heads.forEach(Duke::clearRed);
            noses.forEach(Duke::clearRed);
            bodies.forEach(Duke::clearRed);

            HEADS = heads;
            NOSES = noses;
            BODIES = bodies;

        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public long getSeed() {
        return seed;
    }

    // https://stackoverflow.com/questions/6409652/random-weighted-selection-in-java
    private static final class RandomCollection<E> {
        private final NavigableMap<Double, E> map = new TreeMap<>();
        private final Random random;
        private double total = 0;

        public RandomCollection() {
            this(new Random());
        }

        public RandomCollection(Random random) {
            this.random = random;
        }

        public RandomCollection<E> add(double weight, E result) {
            if (weight <= 0) return this;
            total += weight;
            map.put(total, result);
            return this;
        }

        public E next() {
            double value = random.nextDouble() * total;
            return map.higherEntry(value).getValue();
        }
    }

    private final Long seed;

    public Duke(long seed) {
        this.seed = seed;
    }

    public Duke() {
        this.seed = ThreadLocalRandom.current().nextLong();
    }

    public static Duke from(String text) {
        return new Duke(Hashing.murmur3_128()
                .hashString(text, StandardCharsets.UTF_8).asLong());
    }

    public static Duke from(String text, int rerolls) {
        return new Duke(Hashing.murmur3_128()
                .hashString(text + "##" + Integer.toHexString(rerolls), StandardCharsets.UTF_8).asLong());
    }

    private record Parts(
            BufferedImage body,
            BufferedImage nose,
            BufferedImage head,
            Color noseColor
    ) {}

    private Parts pickParts() {

        var random = new Random();

        random.setSeed(seed);

        var colors = new RandomCollection<Color>(random);
        colors.add(150 * 2, Color.RED);
        colors.add(50 * 2, Color.BLUE);
        colors.add(50 * 2, Color.ORANGE);
        colors.add(50 * 2, Color.MAGENTA);
        colors.add(50 * 2, Color.GREEN);
        colors.add(50 * 2, Color.CYAN);
        colors.add(25, Color.PINK);
        colors.add(25, Color.YELLOW);
        colors.add(1, Color.WHITE);
        colors.add(1, Color.GRAY);
        colors.add(1, Color.LIGHT_GRAY);
        colors.add(1, Color.DARK_GRAY);
        colors.add(1, Color.BLACK);

        /* colors.add(1, new Color(154, 50, 15));
        colors.add(1, new Color(103, 2, 2));
        colors.add(1, new Color(145, 156, 7));
        colors.add(1, new Color(154, 255, 33));
        colors.add(1, new Color(0, 255, 141));
        colors.add(1, new Color(94, 0, 255));
        colors.add(1, new Color(218, 161, 255));
        colors.add(1, new Color(223, 5, 162));
        colors.add(1, new Color(255, 255, 255));
        colors.add(1, new Color(96, 43, 94));
        colors.add(1, new Color(9, 63, 115));
        colors.add(1, new Color(143, 143, 143));
        colors.add(1, new Color(90, 148, 172));
        colors.add(1, new Color(255, 174, 149));
        colors.add(1, new Color(221, 19, 19));
        colors.add(1, new Color(168, 129, 182));
        colors.add(1, new Color(255, 69, 7));
        colors.add(1, new Color(147, 0, 109));
        colors.add(1, new Color(255, 0, 131));
        colors.add(1, new Color(0, 255, 45));
        colors.add(1, new Color(19, 145, 0));
        colors.add(1, new Color(11, 163, 151));
        colors.add(1, new Color(255, 119, 67));
        colors.add(1, new Color(0, 0, 0));
        colors.add(1, new Color(69, 67, 67));
        colors.add(1, new Color(17, 58, 57));
        colors.add(1, new Color(172, 255, 138));
        colors.add(1, new Color(124, 154, 15));
        colors.add(1, new Color(154, 122, 15));
        colors.add(1, new Color(80, 86, 115)); */

        var color = colors.next();

        return new Parts(
                BODIES.get(random.nextInt(0, BODIES.size())),
                NOSES.get(random.nextInt(0, NOSES.size())),
                HEADS.get(random.nextInt(0, HEADS.size())),
                color
        );
    }

    public BufferedImage toBufferedImage_32x32() {
        var parts = pickParts();
        var img = new BufferedImage(
                parts.body.getWidth(),
                parts.body.getHeight(),
                parts.body.getType()
        );

        var g = img.createGraphics();
        doodle(
                g,
                parts.body,
                parts.nose,
                parts.head,
                parts.noseColor
        );

        return img;
    }

    public BufferedImage toBufferedImage_64x64() {
        return Scalr.resize(toBufferedImage_32x32(), Scalr.Method.SPEED, 64, 64);
    }

    public BufferedImage toBufferedImage_128x128() {
        return Scalr.resize(toBufferedImage_32x32(), Scalr.Method.SPEED, 128, 128);
    }

    public BufferedImage toBufferedImage_256x256() {
        return Scalr.resize(toBufferedImage_32x32(), Scalr.Method.SPEED, 256, 256);
    }

    public BufferedImage toBufferedImage_512x512() {
        return Scalr.resize(toBufferedImage_32x32(), Scalr.Method.SPEED, 512, 512);
    }

    public void draw(Graphics2D g) {
        var c = g.getColor();
        var parts = pickParts();

        doodle(
                g,
                parts.body,
                parts.nose,
                parts.head,
                parts.noseColor
        );
        g.setColor(c);
    }

    static void doodle(
            Graphics2D g,
            BufferedImage body,
            BufferedImage nose,
            BufferedImage head,
            Color noseColor
    ) {

        for (int i = 0; i < body.getWidth(); i++) {
            for (int j = 0; j < body.getHeight(); j++) {
                if ((body.getRGB(i, j) & 0xFF000000) != 0) {
                    g.setColor(new Color(body.getRGB(i, j)));
                    g.drawRect(i, j, 1, 1);
                }
            }
        }

        for (int i = 0; i < head.getWidth(); i++) {
            for (int j = 0; j < head.getHeight(); j++) {
                if ((head.getRGB(i, j) & 0xFF000000) != 0) {
                    g.setColor(new Color(head.getRGB(i, j)));
                    g.drawRect(i, j, 1, 1);
                }
            }
        }


        for (int i = 0; i < nose.getWidth(); i++) {
            for (int j = 0; j < nose.getHeight(); j++) {
                if ((nose.getRGB(i, j) & 0xFF000000) != 0) {
                    if (nose.getRGB(i, j) == Color.WHITE.getRGB()) {
                        g.setColor(noseColor);
                    }
                    else {
                        g.setColor(new Color(nose.getRGB(i, j)));
                    }
                    g.drawRect(i, j, 1, 1);
                }
            }
        }

    }

    private static void clearRed(BufferedImage image) {
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                if (image.getRGB(i, j) == Color.RED.getRGB()) {
                    image.setRGB(i, j, 0);
                }
            }
        }
    }

    public static void main(String[] args) {
        var frame = new JFrame();

        var panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));


        var dukeRef = new AtomicReference<BufferedImage>(null);

        class ImagePanel extends JPanel{
            ImagePanel() {
                setSize(new Dimension(260, 260));
            }
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                var duke = dukeRef.get();
                g.drawRoundRect(0, 0, 260, 260, 2, 2);
                if (duke != null) {
                    g.drawImage(duke, 0, 0, this);
                }
            }
        }
        var imagePanel = new ImagePanel();
        panel.add(imagePanel);


        panel.add(Box.createRigidArea(new Dimension(0,5)));

        var button = new JButton();
        button.setText("Random");
        button.addActionListener(a -> {
            dukeRef.set(new Duke().toBufferedImage_256x256());
            SwingUtilities.invokeLater(imagePanel::repaint);
        });
        panel.add(button);

        frame.add(panel);
        frame.setSize(new Dimension(500, 500));
        frame.setVisible(true);
    }


}
