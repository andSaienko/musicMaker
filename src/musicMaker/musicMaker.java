package musicMaker;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.function.Consumer;

public class musicMaker {
    JFrame frame;
    JPanel panel;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    ArrayList<JCheckBox> checkBoxes;

    String[] instrumentsNames = {"bass drum", "closed hi-hat", "open hi-hat", "acoustic snare", "crash cymbal", "hand clap", "high tom", "hi bongo", "maracas", "whistle", "low conga", "cowbell", "vibraslap", "low-mid tom", "high agogo", "open hi conga"};
    int[] instrumentsNum = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 61, 56, 58, 47, 67, 63};
    public static final int checkBoxDimensions = 16;

    public static void main(String[] args) {
        new musicMaker().buildGUI();
    }


    public void buildGUI() {
        frame = new JFrame("Andruha's beat machine");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.setBounds(50, 50, 400, 400);
        frame.setVisible(true);
        frame.pack();

        checkBoxes = new ArrayList<JCheckBox>();
        Box boxButtons = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("start");
        start.addActionListener(new startListener());
        boxButtons.add(start);

        JButton stop = new JButton("stop");
        stop.addActionListener(new stopListener());
        boxButtons.add(stop);

        JButton tUp = new JButton("tempo up");
        tUp.addActionListener(new tUpListener());
        boxButtons.add(tUp);

        JButton tDown = new JButton("tempo down");
        tDown.addActionListener(new tDownListener());
        boxButtons.add(tDown);

        JButton unSelAll = new JButton("clear field");
        unSelAll.addActionListener(new unSelectListener());
        boxButtons.add(unSelAll);

        JButton save = new JButton("save");
        save.addActionListener(new saveListener());
        boxButtons.add(save);

        JButton load = new JButton("load");
        load.addActionListener(new loadListener());
        boxButtons.add(load);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++) {
            nameBox.add(new Label(instrumentsNames[i]));
        }

        background.add(BorderLayout.EAST, boxButtons);
        background.add(BorderLayout.WEST, nameBox);

        frame.getContentPane().add(background);

        GridLayout grid = new GridLayout(16, 16);
        grid.setVgap(1);
        grid.setHgap(2);
        panel = new JPanel(grid);
        background.add(BorderLayout.CENTER, panel);

        for (int i = 0; i < checkBoxDimensions * checkBoxDimensions; i++) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkBoxes.add(c);
            panel.add(c);
        }
        setUpMidi();
    }

    public void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void buildAndRun() {
        int[] trackList = null;

        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) {
            trackList = new int[16];
            int key = instrumentsNum[i];

            for (int j = 0; j < 16; j++) {
                JCheckBox jc = (JCheckBox) checkBoxes.get(j + (16 * i));
                if (jc.isSelected()) {
                    trackList[j] = key;
                } else {
                    trackList[j] = 0;
                }
            }
            makeTracks(trackList);
            track.add(makeEvent(176, 1, 127, 0, 16));
        }
        track.add(makeEvent(192, 9, 1, 0, 15));
        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class startListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            buildAndRun();
        }
    }

    public class stopListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            sequencer.stop();
        }
    }

    public class tUpListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 1.03));
        }
    }

    public class tDownListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 0.97));
        }
    }

    public class unSelectListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            checkBoxes.forEach(new Consumer<JCheckBox>() {
                @Override
                public void accept(JCheckBox jCheckBox) {
                    jCheckBox.setSelected(false);
                }
            });
        }
    }

    public class saveListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            boolean[] checkBoxSaver = new boolean[256];
            for (int i = 0; i < checkBoxSaver.length; i++) {
                JCheckBox check = (JCheckBox) checkBoxes.get(i);
                if (check.isSelected()) {
                    checkBoxSaver[i] = true;
                }
            }
            try {
                FileOutputStream fileStream = new FileOutputStream("song");
                ObjectOutputStream os = new ObjectOutputStream(fileStream);
                os.writeObject(checkBoxSaver);
                os.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public class loadListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                FileInputStream inputStream = new FileInputStream("song");
                ObjectInputStream ois = new ObjectInputStream(inputStream);
                boolean[] checkBoxesInput = null;
                checkBoxesInput = (boolean[]) ois.readObject();
                for (int i = 0; i < checkBoxDimensions * checkBoxDimensions; i++) {
                    JCheckBox chek = checkBoxes.get(i);
                    if (checkBoxesInput[i]) {
                        chek.setSelected(true);
                    } else chek.setSelected(false);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void makeTracks(int[] list) {
        for (int i = 0; i < 16; i++) {
            int key = list[i];

            if (key != 0) {
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i + 1));
            }
        }
    }

    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }
}
