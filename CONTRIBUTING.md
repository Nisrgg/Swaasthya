# Contributing to Swaasthya

Thank you for your interest in contributing to Swaasthya! This document provides guidelines and information for contributors.

## 🤝 How to Contribute

### Reporting Issues
- Use the GitHub issue tracker
- Provide clear description and steps to reproduce
- Include device/OS information for mobile issues
- Add screenshots when relevant

### Suggesting Features
- Check existing issues first
- Provide detailed description of the feature
- Explain the use case and benefits
- Consider implementation complexity

### Code Contributions
1. Fork the repository
2. Create a feature branch from `main`
3. Make your changes
4. Add tests if applicable
5. Ensure all tests pass
6. Submit a pull request

## 🏗️ Development Setup

### Prerequisites
- Android Studio (latest stable)
- Go 1.21 or later
- Docker & Docker Compose
- Git

### Backend Development
```bash
cd backend
docker-compose up -d postgres
go run main.go
```

### Frontend Development
```bash
# Open in Android Studio
# Sync Gradle files
# Run on device/emulator
```

## 📝 Code Standards

### Kotlin/Android
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Follow Material Design guidelines
- Use Jetpack Compose best practices

### Go
- Use `gofmt` for formatting
- Follow [Effective Go](https://golang.org/doc/effective_go.html)
- Add comments for exported functions
- Use meaningful variable names
- Handle errors appropriately

### Git Commits
Use [Conventional Commits](https://www.conventionalcommits.org/):
```
feat: add new feature
fix: fix bug
docs: update documentation
style: formatting changes
refactor: code refactoring
test: add tests
chore: maintenance tasks
```

## 🧪 Testing

### Backend Tests
```bash
cd backend
go test ./...
go test -v ./handlers
```

### Frontend Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## 📋 Pull Request Process

1. **Create Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make Changes**
   - Write clean, readable code
   - Add tests for new functionality
   - Update documentation if needed

3. **Test Your Changes**
   - Run all tests
   - Test on different devices/screen sizes
   - Verify backend API endpoints

4. **Commit Changes**
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   ```

5. **Push and Create PR**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **PR Requirements**
   - Clear title and description
   - Link related issues
   - Add screenshots for UI changes
   - Ensure CI passes

## 🎯 Areas for Contribution

### High Priority
- Appointment booking system
- Push notifications
- Error handling improvements
- Performance optimizations

### Medium Priority
- Additional authentication methods
- Enhanced search functionality
- UI/UX improvements
- Accessibility features

### Low Priority
- Additional languages
- Advanced filtering
- Analytics integration
- Documentation improvements

## 🐛 Bug Reports

When reporting bugs, please include:

### For Mobile Issues
- Device model and Android version
- App version
- Steps to reproduce
- Expected vs actual behavior
- Screenshots/videos
- Logcat output (if relevant)

### For Backend Issues
- API endpoint
- Request/response details
- Error messages
- Server logs
- Environment details

## 💡 Feature Requests

When suggesting features:
- Describe the problem you're solving
- Explain the proposed solution
- Consider alternative approaches
- Discuss implementation complexity
- Provide mockups/wireframes if applicable

## 📚 Resources

### Documentation
- [Android Developer Guide](https://developer.android.com/guide)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Go Documentation](https://golang.org/doc/)
- [Gin Framework](https://gin-gonic.com/docs/)

### Tools
- [Android Studio](https://developer.android.com/studio)
- [GoLand](https://www.jetbrains.com/go/)
- [Postman](https://www.postman.com/) (API testing)
- [Docker](https://www.docker.com/)

## 🏷️ Labels

We use these labels for issues and PRs:
- `bug` - Something isn't working
- `enhancement` - New feature or request
- `documentation` - Documentation improvements
- `good first issue` - Good for newcomers
- `help wanted` - Extra attention needed
- `priority: high` - High priority
- `priority: medium` - Medium priority
- `priority: low` - Low priority

## 📞 Getting Help

- **Discord**: Join our community server
- **Email**: dev@swaasthya.com
- **GitHub Discussions**: Use for general questions
- **Issues**: For bugs and feature requests

## 🙏 Recognition

Contributors will be recognized in:
- README.md contributors section
- Release notes
- Project documentation

Thank you for contributing to Swaasthya! 🎉
