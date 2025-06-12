# Firebase Storage Image Upload Test

## Changes Made

1. **Fixed file naming**: Now uses actual userId (e.g., `john__12-06-25_5:30:15pm`) instead of encoded email
2. **Added folder structure**: Files are now uploaded to `Prem-Noy-Biye/` folder
3. **Proper URL encoding**: Full path including folder is properly URL encoded

## Expected Results

### Before (Old):

- Filename: `a.jpeg` (incorrect)
- Location: Root of storage bucket
- URL: `https://firebasestorage.googleapis.com/v0/b/bingo-9cf4a.appspot.com/o/a.jpeg?alt=media&token=...`

### After (New):

- Filename: `john__12-06-25_5:30:15pm.jpeg` (uses actual userId)
- Location: `Prem-Noy-Biye/john__12-06-25_5:30:15pm.jpeg`
- URL: `https://firebasestorage.googleapis.com/v0/b/bingo-9cf4a.appspot.com/o/Prem-Noy-Biye%2Fjohn__12-06-25_5:30:15pm.jpeg?alt=media&token=...`

## Testing Steps

1. Run the JavaFX app
2. Register a new user (e.g., "John Doe")
3. Complete the stepper and upload an image
4. Check Firebase Storage console - should see the file in `Prem-Noy-Biye/` folder with proper userId naming
5. Try editing profile and uploading a different image - should replace the existing file

## File Structure in Firebase Storage

```
Storage Bucket: bingo-9cf4a.appspot.com
├── Prem-Noy-Biye/
│   ├── john__12-06-25_5:30:15pm.jpeg
│   ├── mary__12-06-25_5:35:22pm.png
│   └── alex__12-06-25_5:40:10pm.jpg
└── (other files)
```
