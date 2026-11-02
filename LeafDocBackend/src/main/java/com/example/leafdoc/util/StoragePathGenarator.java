package com.example.leafdoc.util;

import java.util.UUID;

/**
 * Utility class for generating consistent object paths for images
 * stored in Supabase Storage.
 *
 * <p>All uploaded media is organized under the {@code leaf_doc-media}
 * bucket using a user-based folder hierarchy.</p>
 *
 * <h2>Storage Folder Structure</h2>
 *
 * <pre>
 * leaf_doc-media/
 * └── user/
 *     └── {uid}/
 *         ├── profile.{extension}
 *         ├── plants/
 *         │   └── {plantId}/
 *         │       └── images/
 *         │           └── {UUID}.{extension}
 *         ├── scans/
 *         │   └── {UUID}.{extension}
 *         └── posts/
 *             └── {postId}/
 *                 └── images/
 *                     └── {UUID}.{extension}
 * </pre>
 *
 * <p>Each method generates an object path relative to the
 * {@code leaf_doc-media} bucket. UUID-based filenames ensure
 * unique object names for image collections.</p>
 *
 * <p>This class is stateless and provides static utility methods.
 * It is not intended to be instantiated.</p>
 *
 * @author souptik
 * @since 1.0
 */
public final class StoragePathGenarator {
    private StoragePathGenarator() {    //prevents creating an object
        throw new IllegalStateException("Utility class");
    }

    ///profileImage  |uid        +ext    path: leaf_doc-media/user/{uid}/profile.ext
    ///plantImage    |uid, plId  +ext    path: leaf_doc-media/user/{uid}/plants/{plantId}/images/{UUID}.{ext}
    ///scanImage     |uid        +ext    path: leaf_doc-media/user/{uid}/scans/{UUID}.{ext}
    ///postImage     |uid,postId +ext    path: leaf_doc-media/user/{uid}/posts/{postId}/images/{UUID}.{ext}

    private static final String PROFILE_IMAGE_PATH  = "leaf_doc-media/user/%s/profile.%s";
    private static final String PLANT_IMAGE_PATH    = "leaf_doc-media/user/%s/plants/%s/images/%s.%s";
    private static final String SCAN_IMAGE_PATH     = "leaf_doc-media/user/%s/scans/%s.%s";
    private static final String POST_IMAGE_PATH     = "leaf_doc-media/user/%s/posts/%s/images/%s.%s";


    /**
     * Generates the storage object path for a user's profile picture.
     *
     * <p>Each user has a single profile picture. A fixed filename is
     * used so that uploading a new profile picture replaces the
     * existing one.</p>
     *
     * <p>Generated path:</p>
     * <pre>
     * leaf_doc-media/user/{userId}/profile.{ext}
     * </pre>
     *
     * @param userId the unique identifier of the user
     * @param extension the image file extension
     *            (e.g. {@code .jpg}, {@code .png})
     * @return the generated profile picture object path
     */
    public static String profileImage(Long userId, String extension) {
        return PROFILE_IMAGE_PATH.formatted(userId, extension);
    }

    /**
     * Generates the storage object path for an image belonging
     * to a specific plant owned by a user.
     *
     * <p>A UUID-based filename is generated to support multiple
     * images per plant without filename conflicts.</p>
     *
     * <p>Generated path:</p>
     * <pre>
     * leaf_doc-media/user/{userId}/plants/{plantId}/images/{UUID}.{extension}
     * </pre>
     *
     * @param userId the unique identifier of the plant owner
     * @param plantId the unique identifier of the plant
     * @param extension the image file extension
     *            (e.g. {@code .jpg}, {@code .png})
     * @return the generated plant image object path
     */
    public static String plantImage(Long userId, Long plantId, String extension) {
        return PLANT_IMAGE_PATH.formatted(userId, plantId, UUID.randomUUID().toString(), extension);
    }

    /**
     * Generates the storage object path for a user's scan image.
     *
     * <p>A UUID-based filename allows each user to maintain
     * multiple scan images without filename conflicts.</p>
     *
     * <p>Generated path:</p>
     * <pre>
     * leaf_doc-media/user/{userId}/scans/{UUID}.{extension}
     * </pre>
     *
     * @param userId the unique identifier of the user
     * @param extension the image file extension
     *            (e.g. {@code .jpg}, {@code .png})
     * @return the generated scan image object path
     */
    public static String scanImage(Long userId, String extension) {
        return SCAN_IMAGE_PATH.formatted(userId, UUID.randomUUID().toString(), extension);
    }

    /**
     * Generates the storage object path for an image attached
     * to a specific post created by a user.
     *
     * <p>A UUID-based filename supports multiple images per post
     * without filename conflicts.</p>
     *
     * <p>Generated path:</p>
     * <pre>
     * leaf_doc-media/user/{userId}/posts/{postId}/images/{UUID}.{extension}
     * </pre>
     *
     * @param userId the unique identifier of the post owner
     * @param postId the unique identifier of the post
     * @param extension the image file extension
     *            (e.g. {@code .jpg}, {@code .png})
     * @return the generated post image object path
     */
    public static String postImage(Long userId, Long postId, String extension) {
        return POST_IMAGE_PATH.formatted(userId, postId, UUID.randomUUID().toString(), extension);
    }
}
