import Foundation
import Vision
import AppKit

for i in 1...9 {
    let imgUrl = URL(fileURLWithPath: "manual_page_\(i).png")
    guard let image = NSImage(contentsOf: imgUrl),
          let cgImage = image.cgImage(forProposedRect: nil, context: nil, hints: nil) else {
        continue
    }
    
    let requestHandler = VNImageRequestHandler(cgImage: cgImage, options: [:])
    let request = VNRecognizeTextRequest()
    request.recognitionLevel = .accurate
    request.usesLanguageCorrection = true
    
    try? requestHandler.perform([request])
    
    guard let observations = request.results else { continue }
    let recognizedStrings = observations.compactMap { $0.topCandidates(1).first?.string }
    
    let pageText = recognizedStrings.joined(separator: "\n")
    print("==================== PAGE \(i) ====================")
    print(pageText)
}
