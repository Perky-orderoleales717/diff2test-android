class D2t < Formula
  desc "Diff-driven Android ViewModel test generation CLI"
  homepage "__HOMEPAGE__"
  url "__URL__"
  sha256 "__SHA256__"
  version "__VERSION__"

  depends_on "openjdk@17"

  def install
    libexec.install Dir["*"]
    bin.install_symlink libexec/"bin/d2t"
  end

  test do
    output = shell_output("#{bin}/d2t help")
    assert_match "d2t commands:", output
  end
end
