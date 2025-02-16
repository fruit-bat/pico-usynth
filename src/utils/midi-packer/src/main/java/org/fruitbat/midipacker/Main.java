package org.fruitbat.midipacker;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

public class Main {

	public static void main(String[] args) throws InvalidMidiDataException, IOException {
		final List<String> files = new ArrayList<>();
//		files.add("48709");
//		files.add("pitch_bend_1");
		files.add("bach_example_1");
		//files.add("example");
		// files.add("moonlight");
		// files.add("bwv0248a");
		// files.add("bwv0248b");
		// files.add("deb_clai");
		// files.add("der_winter");
		// files.add("3771vivandte");
		// files.add("beethoven_opus10_1");
		for (final String name : files) {
			tread(name + ".mid");
		}
	}

	private static void tread(final String mname) throws InvalidMidiDataException, IOException {

		try (
				final FileOutputStream fos = new FileOutputStream("output.txt");
				final BufferedOutputStream bos = new BufferedOutputStream(fos);
		) {
			final Syn prsyn = new PrSyn();
			final SynWriter writer = new SynWriter1(new PrintStream(bos));
			final MidiPacker packer = new MidiPacker(writer, 32);

			final Syn syn = new CompSyn(new Syn[] { packer });

			final Sequence sequence = MidiSystem.getSequence(new File(mname));

			writer.writeStart();
			writer.writePPQ(sequence.getResolution());

			final TrackTempo trackTempo = new TrackTempo(sequence, writer);

			final Track[] tracks = sequence.getTracks();
			final List<TrackHandler> trackHandlers = new ArrayList<>();
			for (int i = 0; i < tracks.length; ++i) {
				trackHandlers.add(new TrackHandler(tracks[i], i, syn, trackTempo));
			}

			long tick = 0;
			// long steps = stepsPerTick;
			while (true) {
				boolean active = false;
				writer.writeTime(tick);
				for (int i = 0; i < tracks.length; ++i) {
					final TrackHandler th = trackHandlers.get(i);
					active |= th.active();
					th.tick(tick);
				}
				// System.out.println(packer);
				if (!active)
					break;

				tick++;
				// final long secs = (long)(tick * tickSeconds);
				// System.out.println(String.format("|%10d| %s", secs, prSyn));
				// if (secs > 15) break;
			}

			writer.writeTime(tick);
			writer.writeEnd();
			System.out.println("Done");
			System.out.println("Max voices used " + packer.maxVoiceOnCount());
		}
	}
}
