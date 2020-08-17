/*
 * Copyright 2020-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.example.image.thumbnail.processor;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.function.Function;

import javax.imageio.ImageIO;

public class ThumbnailProcessor implements Function<byte[], byte[]> {
	ImageScaler imageScaler = new ImageScaler();

	public byte[] apply(byte[] bytes) {
		return imageScaler
				.resize(new ByteArrayInputStream(bytes), 100, 100, "JPEG").toByteArray();
	}

	/**
	 * From https://stackoverflow.com/a/39313627/1743446
	 */
	static class ImageScaler {

		public ByteArrayOutputStream resize(InputStream inputStream, int IMG_WIDTH,
				int IMG_HEIGHT, String format) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try {
				BufferedImage originalImage = ImageIO.read(inputStream);
				int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB
						: originalImage.getType();
				BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT,
						type);

				Graphics2D g = resizedImage.createGraphics();
				g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
				g.dispose();
				g.setComposite(AlphaComposite.Src);

				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.setRenderingHint(RenderingHints.KEY_RENDERING,
						RenderingHints.VALUE_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

				ImageIO.write(resizedImage, format, bos);
			}
			catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			return bos;
		}
	}
}
