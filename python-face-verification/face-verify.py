from deepface import DeepFace


result = DeepFace.verify(
    img1_path = r"C:/PythonFaceVerification/kevan2.jpg",
    img2_path = r"C:/PythonFaceVerification/kevan.jpg"
)
print(result)