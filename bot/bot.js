const mineflayer = require('mineflayer')

const CONFIG = {
    host: 'localhost',
    port: 25565,
    username: 'TestBot',
    version: '1.21',
    auth: 'offline'
}

const bot = mineflayer.createBot(CONFIG)

bot.on('login', () => console.log('✅ Bot เชื่อมต่อสำเร็จ!'))
bot.on('spawn', () => {
    console.log('📍 Bot spawn ที่:', bot.entity.position)
    runTests()
})
bot.on('message', (msg) => console.log('💬 Server:', msg.toString()))
bot.on('error', (err) => console.error('❌ Error:', err.message))
bot.on('end', (reason) => console.log('🔌 Disconnected:', reason))

// -------- Test Sequence --------

function runTests() {
    console.log('\n🧪 เริ่มทดสอบ...\n')

    const tests = [
        // --- ทดสอบพื้นฐาน ---
        { delay: 1000,  desc: 'ดูรายการกล่อง loot',       cmd: '/hg listchests' },
        { delay: 2000,  desc: 'ดูคำสั่งทั้งหมด (no args)', cmd: '/hg' },

        // --- ทดสอบ admin (ต้อง OP ก่อน: op TestBot ใน server console) ---
        { delay: 3500,  desc: '[ADMIN] สั่ง airdrop ทันที', cmd: '/hg airdrop' },
        { delay: 5000,  desc: '[ADMIN] reload config',      cmd: '/hg reload' },
        { delay: 6500,  desc: '[ADMIN] ดูกล่องหลัง reload', cmd: '/hg listchests' },
    ]

    tests.forEach(({ delay, desc, cmd }) => {
        setTimeout(() => {
            console.log(`\n▶ ทดสอบ: ${desc}`)
            bot.chat(cmd)
            console.log('📤 ส่ง:', cmd)
        }, delay)
    })

    setTimeout(() => {
        console.log('\n✅ ทดสอบเสร็จแล้ว!')
        console.log('\n📋 สิ่งที่ต้องทดสอบในเกมจริง (ต้องมองไปที่ chest block):')
        console.log('   • /hg addchest    → register กล่อง loot')
        console.log('   • /hg removechest → ลบกล่อง loot')
        console.log('   • เปิดกล่อง       → เช็คว่า loot สุ่มขึ้นมาอัตโนมัติ')
        bot.quit()
    }, 9000)
}
