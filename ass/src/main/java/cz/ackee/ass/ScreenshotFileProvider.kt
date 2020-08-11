package cz.ackee.ass

import androidx.core.content.FileProvider

/**
 * This file provider defines directory in which app can store screenshot images. Referencing
 * multiple file providers in androidManifest where each define {@link FileProvider} as its
 * implementation class leads to compile errors. Simply subclassing and referencing the subclass
 * is enough to fix the problem.
 *
 * Defined by [res/xml/file_provider_paths.xml]
 */
internal class ScreenshotFileProvider : FileProvider()
